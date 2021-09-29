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

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.contract.Contract
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Config
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.isSplitPayment
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInstrumentInputModel
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Action
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Effect
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.State
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCase
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCard
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output
import javax.inject.Inject

internal class PaymentOptionsListBusinessLogic(
    private val showState: suspend (State) -> Action,
    private val showEffect: suspend (Effect) -> Unit,
    private val source: suspend () -> Action,
    private val useCase: PaymentOptionsListUseCase,
    private val paymentParameters: PaymentParameters,
    private val paymentMethodId: String?,
    private val logoutUseCase: LogoutUseCase,
    private val unbindCardUseCase: UnbindCardUseCase,
    private val getConfirmation: GetConfirmation,
    private val shopPropertiesRepository: ShopPropertiesRepository,
    private val configRepository: ConfigRepository
) : Logic<State, Action> {

    private val currentConfig: Config get() = configRepository.getConfig()
    private val currentLogoUrl: String get() = configRepository.getConfig().yooMoneyLogoUrlLight

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.Content -> state.whenContent(action)
        is State.WaitingForAuthState -> state.whenWaitingForAuthState(action)
        is State.ContentWithUnbindingAlert -> state.whenShowUnbindingAlertState(action)
        is State.Error -> state.whenError(action)
    }

    private fun State.Loading.whenLoading(action: Action): Out<State, Action> {
        return when (action) {
            is Action.ConfigLoadFinish -> Out(this) {
                input { useCase.loadPaymentOptions(paymentParameters.amount, paymentMethodId) }
            }
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(currentLogoUrl, action.content)) {
                handlePaymentOptionsListSuccess(action)
            }
            is Action.LoadPaymentOptionListFailed -> Out(State.Error(currentLogoUrl, action.error)) {
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
                val state = takeIf { useCase.isPaymentOptionsActual } ?: State.Loading(currentLogoUrl)
                Out(state) {
                    input { showState(this.state) }
                    input { useCase.loadPaymentOptions(paymentParameters.amount, paymentMethodId) }
                }
            }
            is Action.ProceedWithPaymentMethod -> {
                when (val option = useCase.selectPaymentOption(action.optionId, action.instrumentId)) {
                    is AbstractWallet -> {
                        Out(State.WaitingForAuthState(currentLogoUrl, this)) {
                            output { showEffect(Effect.RequireAuth) }
                            input(source)
                        }
                    }
                    is BankCardPaymentOption -> {
                        val instrumentation =
                            option.paymentInstruments.firstOrNull { it.paymentInstrumentId == action.instrumentId }
                        if (instrumentation != null) {
                            val savePaymentOption = option.savePaymentMethodAllowed && paymentParameters.savePaymentMethod != SavePaymentMethod.OFF
                            val isSplitPayment = shopPropertiesRepository.shopProperties.isSplitPayment
                            if (instrumentation.cscRequired || savePaymentOption || isSplitPayment || option.fee?.service != null) {
                                Out(this) {
                                    output { showEffect(Effect.ShowContract) }
                                    input(source)
                                }
                            } else {
                                Out(this) {
                                    output {
                                        showEffect(
                                            Effect.StartTokenization(
                                                TokenizeInstrumentInputModel(
                                                    paymentOptionId = option.id,
                                                    instrumentBankCard = instrumentation,
                                                    savePaymentMethod = false,
                                                    allowWalletLinking = false,
                                                    confirmation = getConfirmation(option),
                                                    csc = null
                                                )
                                            )
                                        )
                                    }
                                    input(source)
                                }
                            }
                        } else {
                            Out(this) {
                                output { showEffect(Effect.ShowContract) }
                                input(source)
                            }
                        }
                    }
                    else -> {
                        Out(this) {
                            output { showEffect(Effect.ShowContract) }
                            input(source)
                        }
                    }
                }
            }
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(currentLogoUrl, action.content)) {
                handlePaymentOptionsListSuccess(action)
            }
            is Action.Logout -> Out(State.Loading(currentLogoUrl)) {
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
            is Action.OpenUnbindScreen -> {
                when (val paymentOption = useCase.selectPaymentOption(action.optionId, action.instrumentId)) {
                    is LinkedCard -> {
                        Out(this) {
                            output { showEffect(Effect.UnbindLinkedCard(paymentOption)) }
                            input(source)
                        }
                    }
                    is BankCardPaymentOption -> {
                        Out(this) {
                            output {
                                showEffect(Effect.UnbindInstrument(
                                    paymentOption.paymentInstruments.first { it.paymentInstrumentId == action.instrumentId }
                                ))
                            }
                            input(source)
                        }
                    }
                    else -> {
                        Out(this) {
                            input { showState(state) }
                        }
                    }
                }
            }
            is Action.OpenUnbindingAlert ->
                when (val paymentOption = useCase.selectPaymentOption(action.optionId, action.instrumentId)) {
                    is BankCardPaymentOption -> {
                        return if (action.instrumentId.isNullOrEmpty()) {
                            Out(this) {
                                input { showState(state) }
                            }
                        } else {
                            Out(
                                State.ContentWithUnbindingAlert(
                                    currentLogoUrl,
                                    paymentOption.paymentInstruments.first { it.paymentInstrumentId == action.instrumentId },
                                    content,
                                    paymentOption.id,
                                    paymentParameters.amount,
                                    action.instrumentId
                                )
                            ) {
                                input { showState(this.state) }
                            }
                        }
                    }
                    else -> Out.skip(this, source)
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
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(currentLogoUrl, action.content)) {
                when (action.content) {
                    is PaymentOptionListSuccessOutputModel -> {
                        if (action.content.options.find { it is LinkedCard } == null) {
                            output { showEffect(Effect.ShowContract) }
                            input(source)
                        } else {
                            handlePaymentOptionsListSuccess(action)
                        }
                    }
                    is PaymentOptionListNoWalletOutputModel -> input { showState(state) }
                }
            }
            is Action.LoadPaymentOptionListFailed -> Out(State.Error(currentLogoUrl, action.error)) {
                input { showState(this.state) }
            }
            else -> Out(content) {
                input { showState(state) }
            }
        }
    }

    private fun Out.Builder<State.Content, Action>.handlePaymentOptionsListSuccess(action: Action.LoadPaymentOptionListSuccess) {
        if (action.content.options.size == 1) {
            val option = action.content.options.first()
            if (option is BankCardPaymentOption) {
                if (option.paymentInstruments.isEmpty()) {
                    input {
                        Action.ProceedWithPaymentMethod(option.id, null)
                    }
                } else {
                    input { showState(state) }
                }
            } else {
                input {
                    Action.ProceedWithPaymentMethod(option.id, null)
                }
            }
        } else {
            input { showState(state) }
        }
    }

    private fun State.ContentWithUnbindingAlert.whenShowUnbindingAlertState(action: Action): Out<State, Action> {
        return when (action) {
            is Action.ClickOnCancel -> Out(State.Content(currentLogoUrl, content)) {
                input { showState(this.state) }
            }
            is Action.ClickOnUnbind -> Out(this) {
                input { unbindCardUseCase.unbindCard(this.state.instrumentId) }
            }
            is Action.UnbindSuccess -> {
                Out(this) {
                    output { showEffect(Effect.UnbindSuccess(this.state.instrumentBankCard)) }
                    input { useCase.loadPaymentOptions(paymentParameters.amount) }
                }
            }
            is Action.UnbindFailed -> {
                val instrumentBankCard = this.instrumentBankCard
                Out(State.Content(currentLogoUrl, content)) {
                    output { showEffect(Effect.UnbindFailed(instrumentBankCard)) }
                    input(source)
                }
            }
            is Action.LoadPaymentOptionListSuccess -> Out(State.Content(currentLogoUrl, action.content)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Error.whenError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(State.Loading(currentLogoUrl)) {
                input { showState(this.state) }
                input { useCase.loadPaymentOptions(paymentParameters.amount, paymentMethodId) }
            }
            else -> Out.skip(this, source)
        }
    }
}