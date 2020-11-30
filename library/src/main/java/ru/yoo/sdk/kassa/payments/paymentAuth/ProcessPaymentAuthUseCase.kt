/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoo.sdk.kassa.payments.paymentAuth

import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.UseCase
import ru.yoo.sdk.kassa.payments.payment.CurrentUserGateway

internal class ProcessPaymentAuthUseCase(
    private val processPaymentAuthGateway: ProcessPaymentAuthGateway,
    private val currentUserGateway: CurrentUserGateway,
    private val paymentAuthTokenGateway: PaymentAuthTokenGateway
) : UseCase<ProcessPaymentAuthInputModel, ProcessPaymentAuthOutputModel> {

    override fun invoke(inputModel: ProcessPaymentAuthInputModel): ProcessPaymentAuthOutputModel {
        val currentUser = currentUserGateway.currentUser

        check(currentUser is AuthorizedUser) { "can't submit payment auth for anonymous user" }

        return when(inputModel) {
            is RequiredProcessPaymentAuthInputModel -> {
                when (val response = processPaymentAuthGateway.getPaymentAuthToken(currentUser, inputModel.passphrase)) {
                    is PaymentAuthToken -> {
                        handlePaymentAuthToken(inputModel, response)
                        ProcessPaymentAuthSuccessOutputModel()
                    }
                    is PaymentAuthWrongAnswer -> ProcessPaymentAuthWrongAnswerOutputModel(inputModel.passphrase)
                }
            }
            is NotRequiredProcessPaymentAuthInputModel -> {
                when (val response = processPaymentAuthGateway.getPaymentAuthToken(currentUser)) {
                    is PaymentAuthToken -> handlePaymentAuthToken(inputModel, response)
                }
                ProcessPaymentAuthSuccessOutputModel()
            }
        }
    }

    private fun handlePaymentAuthToken(inputModel: ProcessPaymentAuthInputModel, response: PaymentAuthToken) {
        with(paymentAuthTokenGateway) {
            paymentAuthToken = response.token
            if (inputModel.saveAuth) {
                persistPaymentAuth()
            }
        }
    }
}

internal sealed class ProcessPaymentAuthInputModel {
    abstract val saveAuth: Boolean
}

internal data class RequiredProcessPaymentAuthInputModel(
    val passphrase: String,
    override val saveAuth: Boolean
): ProcessPaymentAuthInputModel()

internal data class NotRequiredProcessPaymentAuthInputModel(
    override val saveAuth: Boolean
) : ProcessPaymentAuthInputModel()


internal sealed class ProcessPaymentAuthOutputModel
internal class ProcessPaymentAuthSuccessOutputModel : ProcessPaymentAuthOutputModel()
internal data class ProcessPaymentAuthWrongAnswerOutputModel(
    val passphrase: String
) : ProcessPaymentAuthOutputModel()
