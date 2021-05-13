/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import android.os.Build
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingTool
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.extensions.execute
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutAuthCheckRequest
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutAuthContextGetRequest
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutAuthSessionGenerateRequest
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueExecuteRequest
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueInitRequest
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.map
import java.lang.IllegalStateException
import java.util.concurrent.Semaphore

internal class ApiV3PaymentAuthRepository(
    private val httpClient: Lazy<CheckoutOkHttpClient>,
    private val tokensStorage: TokensStorage,
    private val shopToken: String,
    private val tmxSessionIdStorage: TmxSessionIdStorage,
    private val profilingTool: ProfilingTool,
    private val selectAppropriateAuthType: (AuthType, Array<AuthTypeState>) -> AuthTypeState
) : PaymentAuthTypeRepository, ProcessPaymentAuthRepository, ProfilingTool.SessionIdListener,
    SmsSessionRetryRepository {

    private var processId: String? = null
    private var authContextId: String? = null
    private var authType: AuthType = AuthType.UNKNOWN
    private var tmxSessionId: String? = null
    private val tmxSessionIdSemaphore = Semaphore(0)

    override fun getPaymentAuthToken(
        currentUser: CurrentUser,
        passphrase: String
    ): Result<ProcessPaymentAuthGatewayResponse> {
        val userAuthToken: String = tokensStorage.userAuthToken ?: return Result.Fail(IllegalStateException())
        val currentProcessId: String = processId ?: return Result.Fail(IllegalStateException())

        return when (val result = authCheck(passphrase, userAuthToken)) {
            is Result.Success -> getPaymentAuthToken(currentProcessId, userAuthToken)
            is Result.Fail -> when(result.value) {
                is AuthCheckApiMethodException -> when (result.value.error.errorCode) {
                    ErrorCode.INVALID_ANSWER -> Result.Success(PaymentAuthWrongAnswer(result.value.authState!!))
                    else -> result
                }
                else -> result
            }
        }
    }

    override fun getPaymentAuthToken(currentUser: CurrentUser): Result<ProcessPaymentAuthGatewayResponse> {
        val userAuthToken: String = tokensStorage.userAuthToken ?: return Result.Fail(IllegalStateException())
        val currentProcessId: String = processId ?: return Result.Fail(IllegalStateException())
        return getPaymentAuthToken(currentProcessId, userAuthToken)
    }

    private fun authCheck(passphrase: String, userAuthToken: String): Result<Unit> {
        val currentAuthType = authType.also {
            check(authType != AuthType.UNKNOWN)
        }

        val currentAuthContextId = authContextId ?: return Result.Fail(IllegalStateException())

        val request = CheckoutAuthCheckRequest(
            userAuthToken = userAuthToken,
            shopToken = shopToken,
            answer = passphrase,
            authType = currentAuthType,
            authContextId = currentAuthContextId
        )

        return httpClient.value.execute(request)
    }

    private fun tokenIssueExecute(currentProcessId: String, userAuthToken: String): Result<String> {
        val request = CheckoutTokenIssueExecuteRequest(currentProcessId, userAuthToken, shopToken)
        return httpClient.value.execute(request)
    }

    private fun getPaymentAuthToken(currentProcessId: String, userAuthToken: String): Result<PaymentAuthToken> {
        return when (val result = tokenIssueExecute(currentProcessId, userAuthToken)) {
            is Result.Success -> Result.Success(PaymentAuthToken(result.value))
            is Result.Fail -> result
        }
    }

    override fun getPaymentAuthType(linkWalletToApp: Boolean, amount: Amount): Result<AuthTypeState> {
        processId = null
        authContextId = null
        authType = AuthType.UNKNOWN

        val userAuthToken: String = tokensStorage.userAuthToken ?: return Result.Fail(IllegalStateException())
        return when(val result = tokenIssueInit(userAuthToken, amount, linkWalletToApp)) {
            is Result.Success -> when(result.value) {
                is CheckoutTokenIssueInitResponse.Success -> {
                    this.processId = result.value.processId
                    Result.Success(AuthTypeState.NotRequired)
                }
                is CheckoutTokenIssueInitResponse.AuthRequired -> handleAuthRequired(userAuthToken, result.value)
            }
            is Result.Fail -> result
        }
    }

    private fun handleAuthRequired(
        userAuthToken: String,
        tokenIssueInitResponse: CheckoutTokenIssueInitResponse.AuthRequired
    ): Result<AuthTypeState> {
        this.processId = tokenIssueInitResponse.processId
        this.authContextId = tokenIssueInitResponse.authContextId

        val localAuthContextId: String = authContextId ?: return Result.Fail(IllegalStateException())
        val authTypeState = when (val result = authContextGet(localAuthContextId, userAuthToken)) {
            is Result.Success -> result.value
            is Result.Fail -> return result
        }
        authType = authTypeState.type

        val updatedAuthTypeState = when (val result = authSessionGenerate(userAuthToken)) {
            is Result.Success -> result.value
            else -> return result
        }
        authType = updatedAuthTypeState.type

        return Result.Success(updatedAuthTypeState)
    }

    private fun tokenIssueInit(
        userAuthToken: String,
        amount: Amount,
        multipleUsage: Boolean
    ): Result<CheckoutTokenIssueInitResponse> {
        tmxSessionId = tmxSessionIdStorage.tmxSessionId

        if (tmxSessionId.isNullOrEmpty()) {
            profilingTool.requestSessionId(this)
            tmxSessionIdSemaphore.acquire()
        }

        tmxSessionId ?: return Result.Fail(IllegalStateException())

        val request = CheckoutTokenIssueInitRequest(
            instanceName = Build.MANUFACTURER + ", " + Build.MODEL,
            singleAmountMax = amount,
            multipleUsage = multipleUsage,
            tmxSessionId = checkNotNull(tmxSessionId),
            shopToken = shopToken,
            userAuthToken = userAuthToken
        )
        return httpClient.value.execute(request)
    }

    private fun authContextGet(localAuthContextId: String, userAuthToken: String): Result<AuthTypeState> {
        val request = CheckoutAuthContextGetRequest(localAuthContextId, userAuthToken, shopToken)
        return httpClient.value.execute(request).map {
            selectAppropriateAuthType(it.defaultAuthType, it.authTypeStates)
        }
    }

    private fun authSessionGenerate(userAuthToken: String): Result<AuthTypeState> {
        val currentAuthType = authType.takeIf { it != AuthType.UNKNOWN } ?: return Result.Fail(IllegalStateException())
        val currentAuthContextId = authContextId ?: return Result.Fail(IllegalStateException())
        val request = CheckoutAuthSessionGenerateRequest(
            authType = currentAuthType,
            authContextId = currentAuthContextId,
            shopToken = shopToken,
            userAuthToken = userAuthToken
        )
        return httpClient.value.execute(request)
    }

    override fun retrySmsSession(): Result<AuthTypeState> {
        val userAuthToken: String = tokensStorage.userAuthToken ?: return Result.Fail(IllegalStateException())
        return authSessionGenerate(userAuthToken)
    }

    override fun onProfilingSessionId(sessionId: String) {
        tmxSessionId = sessionId
        tmxSessionIdSemaphore.release()
    }

    override fun onProfilingError(status: String) {
        tmxSessionId = status
        tmxSessionIdSemaphore.release()
    }
}
