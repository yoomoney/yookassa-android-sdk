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

package ru.yoo.sdk.kassa.payments.contract

import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel

internal object Contract {

    sealed class State {
        object Loading : State() {
            override fun toString() = "State.Loading"
        }

        data class Error(val error: Throwable) : State()

        data class Content(
            val shopTitle: CharSequence,
            val shopSubtitle: CharSequence,
            val paymentOption: PaymentOption,
            val savePaymentMethod: Boolean,
            val showAllowWalletLinking: Boolean,
            val allowWalletLinking: Boolean,
            val showPhoneInput: Boolean
        ) : State()

        data class GooglePay(
            val content: Content,
            val paymentOptionId: Int
        ) : State()

        data class Tokenize(val content: Content, val paymentOptionInfo: PaymentOptionInfo?): State()

        data class TokenizeError(
            val content: Content,
            val paymentOptionInfo: PaymentOptionInfo?,
            val error: Throwable
        ): State()
    }

    sealed class Action {
        object Load : Action()
        data class LoadContractFailed(val error: Throwable) : Action()
        data class LoadContractSuccess(val outputModel: SelectedPaymentOptionOutputModel) : Action()
        data class Tokenize(val paymentOptionInfo: PaymentOptionInfo? = null): Action()
        data class TokenizeFailed(val error: Throwable) : Action()
        data class TokenizeSuccess(val content: TokenizeOutputModel) : Action()
        object RestartProcess: Action() {
            override fun toString() = "Action.RestartProcess"
        }
        data class ChangeSavePaymentMethod(val savePaymentMethod: Boolean): Action()
        data class ChangeAllowWalletLinking(val isAllowed: Boolean): Action()
        data class PaymentAuthRequired(val charge: Amount): Action()
        object PaymentAuthSuccess: Action()
        object PaymentAuthCancel: Action()
        object Logout: Action()
        object LogoutSuccessful: Action()
        object GooglePayCancelled: Action()
    }

    sealed class Effect {
        object RestartProcess: Effect() {
            override fun toString() = "Effect.RestartProcess"
        }
        object CancelProcess: Effect() {
            override fun toString() = "Effect.CancelProcess"
        }
        data class TokenizeComplete(val tokenizeOutputModel: TokenizeOutputModel) : Effect()
        data class PaymentAuthRequired(val charge: Amount): Effect()
    }
}