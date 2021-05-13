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

import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Action
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Effect
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.State
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class PaymentAuthBusinessLogic(
    val showState: suspend (State) -> Action,
    val showEffect: suspend (Effect) -> Unit,
    val source: suspend () -> Action,
    private val requestPaymentAuthUseCase: RequestPaymentAuthUseCase,
    private val processPaymentAuthUseCase: ProcessPaymentAuthUseCase
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.StartError -> state.whenStartError(action)
        is State.InputCode -> state.whenInputCode(action)
        is State.InputCodeProcess -> state.whenInputCodeProcess(action)
        is State.InputCodeVerifyExceeded -> state.whenInputCodeVerifyExceeded(action)
        is State.ProcessError -> state.whenProcessError(action)
    }

    private fun State.Loading.whenLoading(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Start -> Out(this) {
                input { requestPaymentAuthUseCase.startPaymentAuth(action.linkWalletToApp, action.amount) }
            }
            is Action.StartSuccess -> Out(State.InputCode(action.authTypeState)) {
                input { showState(this.state) }
            }
            is Action.StartFailed -> Out(State.StartError(action.error)) {
                input { showState(this.state) }
            }
            is Action.ProcessAuthNotRequired -> Out(this) {
                input { processPaymentAuthUseCase.process(null, action.linkWalletToApp) }
            }
            is Action.ProcessAuthSuccess -> Out(this) {
                output { showEffect(Effect.ShowSuccess) }
            }
            is Action.ProcessAuthFailed -> Out(State.StartError(action.error)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.InputCode.whenInputCode(action: Action): Out<State, Action> {
        return when (action) {
            is Action.ProcessAuthRequired -> Out(State.InputCodeProcess(action.passphrase, this.data)) {
                input { showState(this.state) }
                input { processPaymentAuthUseCase.process(action.passphrase, action.linkWalletToApp) }
            }
            is Action.Start -> Out(State.Loading) {
                input { showState(this.state) }
                input { requestPaymentAuthUseCase.startPaymentAuth(action.linkWalletToApp, action.amount) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.InputCodeProcess.whenInputCodeProcess(action: Action): Out<State, Action> {
        return when(action) {
            is Action.ProcessAuthWrongAnswer -> Out(State.InputCode(action.authTypeState)) {
                input { showState(this.state) }
                output { showEffect(Effect.ProcessAuthWrongAnswer(
                    attemptsCount = action.authTypeState.attemptsCount,
                    attemptsLeft = action.authTypeState.attemptsLeft
                )) }
            }
            is Action.ProcessAuthSuccess -> Out(this) {
                output { showEffect(Effect.ShowSuccess) }
            }
            is Action.ProcessAuthFailed -> Out(State.ProcessError(this.data, action.error)) {
                input { showState(this.state) }
            }
            is Action.ProcessAuthVerifyExceeded -> Out(State.InputCodeVerifyExceeded(this.passphrase, this.data)) {
                input { showState(this.state) }
            }
            is Action.ProcessAuthSessionBroken -> Out(State.StartError(action.error)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.InputCodeVerifyExceeded.whenInputCodeVerifyExceeded(action: Action): Out<State, Action> {
        return when(action) {
            is Action.Start -> Out(State.Loading) {
                input { showState(this.state) }
                input { requestPaymentAuthUseCase.startPaymentAuth(action.linkWalletToApp, action.amount) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.StartError.whenStartError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Start -> Out(State.Loading) {
                input { showState(this.state) }
                input { requestPaymentAuthUseCase.startPaymentAuth(action.linkWalletToApp, action.amount) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.ProcessError.whenProcessError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.StartSuccess -> Out(State.InputCode(action.authTypeState)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }
}