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

package ru.yandex.money.android.sdk.paymentAuth

import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.model.UseCase
import ru.yandex.money.android.sdk.payment.CurrentUserGateway

internal class ProcessPaymentAuthUseCase(
    private val processPaymentAuthGateway: ProcessPaymentAuthGateway,
    private val currentUserGateway: CurrentUserGateway,
    private val paymentAuthTokenGateway: PaymentAuthTokenGateway
) : UseCase<ProcessPaymentAuthInputModel, ProcessPaymentAuthOutputModel> {

    override fun invoke(inputModel: ProcessPaymentAuthInputModel): ProcessPaymentAuthOutputModel {
        val currentUser = currentUserGateway.currentUser

        check(currentUser is AuthorizedUser) { "can't submit payment auth for anonymous user" }

        val response = processPaymentAuthGateway.getPaymentAuthToken(currentUser, inputModel.passphrase)

        return when (response) {
            is PaymentAuthToken -> {
                with(paymentAuthTokenGateway) {
                    paymentAuthToken = response.token

                    if (inputModel.saveAuth) {
                        persistPaymentAuth()
                    }
                }

                ProcessPaymentAuthSuccessOutputModel()
            }
            is PaymentAuthWrongAnswer -> ProcessPaymentAuthWrongAnswerOutputModel(inputModel.passphrase)
        }
    }
}

internal data class ProcessPaymentAuthInputModel(
    val passphrase: String,
    val saveAuth: Boolean
)

internal sealed class ProcessPaymentAuthOutputModel
internal class ProcessPaymentAuthSuccessOutputModel : ProcessPaymentAuthOutputModel()
internal data class ProcessPaymentAuthWrongAnswerOutputModel(
    val passphrase: String
) : ProcessPaymentAuthOutputModel()
