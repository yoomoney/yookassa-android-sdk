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

package ru.yoo.sdk.kassa.payments.paymentOptionList

import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Action
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Effect
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionList.State
import ru.yoo.sdk.march.Logic
import ru.yoo.sdk.march.Out
import ru.yoo.sdk.march.input
import ru.yoo.sdk.march.output

internal class PaymentOptionsListBusinessLogic(
    val showState: suspend (State) -> Action,
    val showEffect: suspend (Effect) -> Unit,
    val source: suspend () -> Action,
    val useCase: PaymentOptionsListUseCase,
    val paymentParameters: PaymentParameters,
    val logoutUseCase: LogoutUseCase
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.Content -> state.whenContent(action)
        is State.WaitingForAuthState -> state.whenWaitingForAuthState(action)
        is State.Error -> state.whenError(action)
    }

    private fun State.Loading.whenLoading(action: Action): Out<State, Action> {
        return when (action) {
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(action.content)) {
                input { showState(this.state) }
            }
            is Action.LoadPaymentOptionListFailed -> Out(State.Error(action.error)) {
                input { showState(this.state) }
            }
            is Action.Logout -> Out(this) {
                input {
                    logoutUseCase.logout()
                    Action.LogoutSuccessful
                }
                input { useCase.loadPaymentOptions(paymentParameters.amount) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Content.whenContent(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> {
                val state = takeIf { useCase.isPaymentOptionsActual } ?: State.Loading
                Out(state) {
                    input { showState(this.state) }
                    input { useCase.loadPaymentOptions(action.amount, action.paymentMethodId) }
                }
            }
            is Action.ProceedWithPaymentMethod -> {
                when (useCase.selectPaymentOption(action.optionId)) {
                    is AbstractWallet -> {
                        Out(State.WaitingForAuthState(this)) {
                            output { showEffect(Effect.RequireAuth) }
                            input(source)
                        }
                    }
                    else -> {
                        Out(this) {
                            output { showEffect(Effect.ProceedWithPaymentMethod) }
                            input(source)
                        }
                    }
                }

            }
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(action.content)) {
                input { showState(this.state) }
            }
            is Action.Logout -> Out(State.Loading) {
                input {
                    logoutUseCase.logout()
                    if (paymentParameters.paymentMethodTypes.size == 1) {
                        showEffect(Effect.Cancel)
                        Action.LogoutSuccessful
                    } else {
                        useCase.loadPaymentOptions(paymentParameters.amount)
                    }
                }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.WaitingForAuthState.whenWaitingForAuthState(action: Action): Out<State, Action> {
        return when (action) {
            is Action.PaymentAuthSuccess -> Out(this) {
                input { useCase.loadPaymentOptions(paymentParameters.amount) }
            }
            is Action.PaymentAuthCancel -> Out(content) {
                if (paymentParameters.paymentMethodTypes.size == 1) {
                    output { showEffect(Effect.Cancel) }
                } else {
                    input { useCase.loadPaymentOptions(paymentParameters.amount) }
                }
            }
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(action.content)) {
                when(action.content) {
                    is PaymentOptionListSuccessOutputModel -> {
                        if (action.content.options.find { it is LinkedCard } == null) {
                            output { showEffect(Effect.ProceedWithPaymentMethod) }
                            input(source)
                        } else {
                            input { showState(state) }
                        }
                    }
                    is PaymentOptionListNoWalletOutputModel -> input { showState(state) }
                }
            }
            is Action.LoadPaymentOptionListFailed -> Out(State.Error(action.error)) {
                input { showState(this.state) }
            }
            else -> Out(content) {
                input { showState(state) }
            }
        }
    }

    private fun State.Error.whenError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(State.Loading) {
                input { showState(this.state) }
                input { useCase.loadPaymentOptions(action.amount, action.paymentMethodId) }
            }
            else -> Out.skip(this, source)
        }
    }
}