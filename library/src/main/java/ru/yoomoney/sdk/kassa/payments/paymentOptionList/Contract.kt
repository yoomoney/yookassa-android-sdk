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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount

internal object PaymentOptionList {
    sealed class State {
        object Loading : State() {
            override fun toString() = "State.Loading"
        }

        data class Error(val error: Throwable) : State()

        data class Content(val content: PaymentOptionListOutputModel) : State()

        data class WaitingForAuthState(val content: Content): State()
    }

    sealed class Action {
        data class Load(val amount: Amount, val paymentMethodId: String?): Action()
        object Logout: Action()
        data class ProceedWithPaymentMethod(val optionId: Int): Action()
        data class LoadPaymentOptionListFailed(val error: Throwable): Action()
        data class LoadPaymentOptionListSuccess(val content: PaymentOptionListOutputModel): Action()
        object PaymentAuthSuccess: Action()
        object PaymentAuthCancel: Action()
        object LogoutSuccessful: Action()
    }

    sealed class Effect {
        object ProceedWithPaymentMethod: Effect()
        object RequireAuth: Effect()
        object Cancel: Effect()
    }
}