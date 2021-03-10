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

package ru.yoo.sdk.kassa.payments.paymentAuth

import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.model.AuthTypeState

internal object PaymentAuth {
    sealed class State {
        object Loading : State() {
            override fun toString() = "State.Loading"
        }

        data class StartError(val error: Throwable) : State()
        data class InputCode(val data: AuthTypeState.SMS) : State()
        data class InputCodeProcess(val passphrase: String, val data: AuthTypeState.SMS): State()
        data class InputCodeVerifyExceeded(val passphrase: String, val data: AuthTypeState.SMS): State()
        data class ProcessError(val data: AuthTypeState.SMS, val error: Throwable) : State()
    }

    sealed class Action {
        data class Start(val linkWalletToApp: Boolean, val amount: Amount) : Action()
        data class StartFailed(val error: Throwable) : Action()
        data class StartSuccess(val authTypeState: AuthTypeState.SMS) : Action()

        data class ProcessAuthRequired(
            val passphrase: String,
            val linkWalletToApp: Boolean
        ) : Action()
        data class ProcessAuthNotRequired(val linkWalletToApp: Boolean) : Action() {
            override fun toString() = "Action.ProcessAuthNotRequired"
        }
        object ProcessAuthSuccess: Action() {
            override fun toString() = "Action.ProcessAuthSuccess"
        }
        data class ProcessAuthFailed(val error: Throwable): Action()
        data class ProcessAuthWrongAnswer(val authTypeState: AuthTypeState.SMS): Action()
        object ProcessAuthVerifyExceeded: Action()
        data class ProcessAuthSessionBroken(val error: Throwable): Action()
    }

    sealed class Effect {
        data class ProcessAuthWrongAnswer(
            val attemptsCount: Int?,
            val attemptsLeft: Int?
        ): Effect() {
            override fun toString() = "Effect.ProcessAuthWrongAnswer"
        }
        object ShowSuccess : Effect() {
            override fun toString() = "Effect.ShowSuccess"
        }
    }
}