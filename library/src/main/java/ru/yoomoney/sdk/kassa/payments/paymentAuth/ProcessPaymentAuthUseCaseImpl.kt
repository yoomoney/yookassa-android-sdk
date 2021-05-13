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

import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository

internal class ProcessPaymentAuthUseCaseImpl(
    private val processPaymentAuthRepository: ProcessPaymentAuthRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val paymentAuthTokenRepository: PaymentAuthTokenRepository,
    private val errorReporter: ErrorReporter
) : ProcessPaymentAuthUseCase {

    override suspend fun process(passphrase: String?, linkWalletToApp: Boolean): PaymentAuth.Action {
        val currentUser = currentUserRepository.currentUser
        try {
            check(currentUser is AuthorizedUser) { "can't submit payment auth for anonymous user" }
        } catch (exception: IllegalStateException) {
            errorReporter.report(SdkException(exception))
            return PaymentAuth.Action.ProcessAuthFailed(exception)
        }
        return if (passphrase != null) {
            when (val result = processPaymentAuthRepository.getPaymentAuthToken(currentUser, passphrase)) {
                is Result.Success -> {
                    when (val response = result.value) {
                        is PaymentAuthToken -> {
                            handlePaymentAuthToken(linkWalletToApp, response)
                            PaymentAuth.Action.ProcessAuthSuccess
                        }
                        is PaymentAuthWrongAnswer -> {
                            if (response.data is AuthTypeState.SMS) {
                                if (response.data.attemptsLeft != 0) {
                                    PaymentAuth.Action.ProcessAuthWrongAnswer(response.data)
                                } else {
                                    PaymentAuth.Action.ProcessAuthVerifyExceeded
                                }
                            } else {
                                PaymentAuth.Action.ProcessAuthFailed(IllegalStateException("Auth type: ${response.data} not supported"))
                            }
                        }
                    }
                }
                is Result.Fail -> {
                    when (result.value) {
                        is ApiMethodException -> when (result.value.error.errorCode) {
                            ErrorCode.VERIFY_ATTEMPTS_EXCEEDED -> PaymentAuth.Action.ProcessAuthVerifyExceeded
                            ErrorCode.INVALID_CONTEXT, ErrorCode.SESSION_DOES_NOT_EXIST,
                            ErrorCode.SESSION_EXPIRED, ErrorCode.UNSUPPORTED_AUTH_TYPE -> PaymentAuth.Action.ProcessAuthSessionBroken(result.value)
                            else -> PaymentAuth.Action.ProcessAuthFailed(result.value)
                        }
                        else -> PaymentAuth.Action.ProcessAuthFailed(result.value)
                    }
                }
            }
        } else {
            when (val result = processPaymentAuthRepository.getPaymentAuthToken(currentUser)) {
                is Result.Success -> {
                    if (result.value is PaymentAuthToken) {
                        handlePaymentAuthToken(linkWalletToApp, result.value)
                    }
                    PaymentAuth.Action.ProcessAuthSuccess
                }
                is Result.Fail -> PaymentAuth.Action.ProcessAuthFailed(result.value)
            }
        }
    }

    private fun handlePaymentAuthToken(linkWalletToApp: Boolean, response: PaymentAuthToken) {
        with(paymentAuthTokenRepository) {
            paymentAuthToken = response.token
            if (linkWalletToApp) {
                persistPaymentAuth()
            }
        }
    }
}