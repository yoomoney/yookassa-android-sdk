/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.impl.paymentAuth

import android.os.Build
import okhttp3.OkHttpClient
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.AuthType
import ru.yandex.money.android.sdk.AuthTypeState
import ru.yandex.money.android.sdk.CurrentUser
import ru.yandex.money.android.sdk.ErrorCode
import ru.yandex.money.android.sdk.impl.ApiMethodException
import ru.yandex.money.android.sdk.impl.ProfilingTool
import ru.yandex.money.android.sdk.impl.ThreatMetrixProfilingTool
import ru.yandex.money.android.sdk.impl.TokensStorage
import ru.yandex.money.android.sdk.impl.extensions.execute
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthCheckRequest
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthContextGetRequest
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthSessionGenerateRequest
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueExecuteRequest
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueInitRequest
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthToken
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTypeGateway
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthWrongAnswer
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthGateway
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthGatewayResponse
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryGateway
import java.util.concurrent.Semaphore

internal class ApiV3PaymentAuthGateway(
        private val httpClient: Lazy<OkHttpClient>,
        private val tokensStorage: TokensStorage,
        private val shopToken: String,
        private val tmxProfilingTool: ThreatMetrixProfilingTool,
        private val selectAppropriateAuthType: (AuthType, Array<AuthTypeState>) -> AuthTypeState
) : PaymentAuthTypeGateway, ProcessPaymentAuthGateway, ProfilingTool.SessionIdListener, SmsSessionRetryGateway {

    private var processId: String? = null
    private var authContextId: String? = null
    private var authType: AuthType = AuthType.UNKNOWN
    private var tmxSessionId: String? = null
    private val tmxSessionIdSemaphore = Semaphore(0)

    override fun getPaymentAuthToken(currentUser: CurrentUser, passphrase: String): ProcessPaymentAuthGatewayResponse {
        val userAuthToken: String = checkNotNull(tokensStorage.userAuthToken)
        val currentProcessId: String = checkNotNull(processId)

        return if (authCheck(passphrase, userAuthToken)) {
            PaymentAuthToken(tokenIssueExecute(currentProcessId, userAuthToken))
        } else {
            PaymentAuthWrongAnswer()
        }
    }

    private fun authCheck(passphrase: String, userAuthToken: String): Boolean {
        val currentAuthType = authType.also {
            check(authType != AuthType.UNKNOWN)
        }

        val currentAuthContextId = checkNotNull(authContextId)

        val request = CheckoutAuthCheckRequest(
                userAuthToken = userAuthToken,
                shopToken = shopToken,
                answer = passphrase,
                authType = currentAuthType,
                authContextId = currentAuthContextId
        )

        val response = httpClient.value.execute(request)

        return when (response.errorCode) {
            null -> true
            ErrorCode.INVALID_ANSWER -> false
            else -> throw ApiMethodException(response.errorCode)
        }
    }

    private fun tokenIssueExecute(currentProcessId: String, userAuthToken: String): String {
        val request = CheckoutTokenIssueExecuteRequest(currentProcessId, userAuthToken, shopToken)
        val response = httpClient.value.execute(request)

        if (response.errorCode != null) {
            throw ApiMethodException(response.errorCode)
        }

        return checkNotNull(response.accessToken)
    }

    override fun getPaymentAuthType(linkWalletToApp: Boolean, amount: Amount): AuthTypeState {
        processId = null
        authContextId = null
        authType = AuthType.UNKNOWN

        val userAuthToken: String = checkNotNull(tokensStorage.userAuthToken)
        val tokenIssueInitResponse = tokenIssueInit(userAuthToken, amount, linkWalletToApp)

        processId = tokenIssueInitResponse.processId
        authContextId = tokenIssueInitResponse.authContextId

        val localAuthContextId: String = checkNotNull(authContextId)
        val authTypeState = authContextGet(localAuthContextId, userAuthToken)
        authType = authTypeState.type

        val updatedAuthTypeState = authSessionGenerate(userAuthToken)
        authType = updatedAuthTypeState.type

        return updatedAuthTypeState
    }

    private fun tokenIssueInit(
            userAuthToken: String,
            amount: Amount,
            multipleUsage: Boolean
    ): CheckoutTokenIssueInitResponse {
        tmxProfilingTool.requestSessionId(this)
        tmxSessionIdSemaphore.acquire()

        val request = CheckoutTokenIssueInitRequest(
                instanceName = Build.MANUFACTURER + ", " + Build.MODEL,
                singleAmountMax = amount,
                multipleUsage = multipleUsage,
                tmxSessionId = checkNotNull(tmxSessionId),
                shopToken = shopToken,
                userAuthToken = userAuthToken
        )
        val response = httpClient.value.execute(request)

        if (response.errorCode != null) {
            throw ApiMethodException(response.errorCode)
        }

        checkNotNull(response.authContextId)
        checkNotNull(response.processId)

        return response
    }

    private fun authContextGet(localAuthContextId: String, userAuthToken: String): AuthTypeState {
        val request = CheckoutAuthContextGetRequest(localAuthContextId, userAuthToken, shopToken)
        val response = httpClient.value.execute(request)

        if (response.errorCode != null) {
            throw ApiMethodException(response.errorCode)
        }

        return selectAppropriateAuthType(response.defaultAuthType, response.authTypeStates)
    }

    private fun authSessionGenerate(userAuthToken: String): AuthTypeState {
        val currentAuthType = authType.also {
            check(authType != AuthType.UNKNOWN)
        }

        val currentAuthContextId = checkNotNull(authContextId)

        val request = CheckoutAuthSessionGenerateRequest(
                authType = currentAuthType,
                authContextId = currentAuthContextId,
                shopToken = shopToken,
                userAuthToken = userAuthToken
        )

        val response = httpClient.value.execute(request)

        if (response.errorCode != null) {
            throw ApiMethodException(response.errorCode)
        }

        return checkNotNull(response.authTypeState)
    }

    override fun retrySmsSession(): AuthTypeState {
        val userAuthToken: String = checkNotNull(tokensStorage.userAuthToken)
        return authSessionGenerate(userAuthToken)
    }

    override fun onProfilingSessionId(sessionId: String) {
        tmxSessionId = sessionId
        tmxSessionIdSemaphore.release()
    }

    override fun onProfilingError() {
        tmxSessionId = null
        tmxSessionIdSemaphore.release()
    }
}
