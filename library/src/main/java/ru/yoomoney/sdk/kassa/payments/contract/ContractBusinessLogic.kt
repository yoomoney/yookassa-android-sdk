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

package ru.yoomoney.sdk.kassa.payments.contract

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.isNullOrZero
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Effect
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output
import ru.yoomoney.sdk.kassa.payments.model.Confirmation

internal class ContractBusinessLogic(
    val showState: suspend (State) -> Action,
    val showEffect: suspend (Effect) -> Unit,
    val source: suspend () -> Action,
    private val paymentParameters: PaymentParameters,
    private val selectPaymentOptionUseCase: SelectPaymentOptionUseCase,
    private val tokenizeUseCase: TokenizeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getConfirmation: (PaymentOption) -> Confirmation
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.Content -> state.whenContent(action)
        is State.Tokenize -> state.whenTokenize(action)
        is State.TokenizeError -> state.whenTokenizeError(action)
        is State.GooglePay -> state.whenGooglePay(action)
        is State.Error -> state.whenError(action)
    }

    private fun State.Loading.whenLoading(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(this) {
                input { selectPaymentOptionUseCase.select() }
            }
            is Action.LoadContractSuccess -> action.whenContractSuccess()
            is Action.LoadContractFailed -> Out(State.Error(action.error)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Content.whenContent(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(State.Loading) {
                input { showState(this.state) }
                input { selectPaymentOptionUseCase.select() }
            }
            is Action.Tokenize -> Out(State.Tokenize(this, action.paymentOptionInfo)) {
                input { showState(this.state) }
                input { tokenizeUseCase.tokenize(this.state.content.getTokenizeInputModel(action.paymentOptionInfo)) }
            }
            is Action.RestartProcess -> Out(this) {
                input(source)
                output { showEffect(Effect.RestartProcess) }
            }
            is Action.Logout -> Out(this) {
                input {
                    logoutUseCase.logout()
                    if (paymentParameters.paymentMethodTypes.size == 1) {
                        showEffect(Effect.CancelProcess)
                    } else {
                        showEffect(Effect.RestartProcess)
                    }
                    Action.LogoutSuccessful
                }
            }
            is Action.ChangeAllowWalletLinking -> Out(this.copy(allowWalletLinking = action.isAllowed)) {
                input(source)
            }
            is Action.ChangeSavePaymentMethod -> Out(this.copy(savePaymentMethod = action.savePaymentMethod)) {
                input(source)
            }
            is Action.GooglePayCancelled -> Out(State.Loading) {
                input { selectPaymentOptionUseCase.select() }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Tokenize.whenTokenize(action: Action): Out<State, Action> {
        return when (action) {
            is Action.RestartProcess -> Out(this) {
                input(source)
                output { showEffect(Effect.RestartProcess) }
            }
            is Action.PaymentAuthRequired -> Out(this) {
                input(source)
                output { showEffect(Effect.PaymentAuthRequired(action.charge)) }
            }
            is Action.PaymentAuthSuccess -> Out(this) {
                input { tokenizeUseCase.tokenize(this.state.content.getTokenizeInputModel(this.state.paymentOptionInfo)) }
            }
            is Action.PaymentAuthCancel -> Out(this.content) {
                input { showState(this.state) }
            }

            is Action.TokenizeSuccess -> Out(this) {
                input { showState(this.state) }
                output { showEffect(Effect.TokenizeComplete(action.content)) }
            }
            is Action.TokenizeFailed -> Out(State.TokenizeError(this.content, this.paymentOptionInfo, action.error)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.TokenizeError.whenTokenizeError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Tokenize -> Out(State.Tokenize(this.content, action.paymentOptionInfo)) {
                input { showState(this.state) }
                input { tokenizeUseCase.tokenize(this.state.content.getTokenizeInputModel(action.paymentOptionInfo)) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.GooglePay.whenGooglePay(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Tokenize -> Out(State.Tokenize(this.content, action.paymentOptionInfo)) {
                input { showState(this.state) }
                input { tokenizeUseCase.tokenize(this.state.content.getTokenizeInputModel(action.paymentOptionInfo)) }
            }
            is Action.GooglePayCancelled -> Out(this) {
                if (paymentParameters.paymentMethodTypes.size == 1) {
                    output { showEffect(Effect.CancelProcess) }
                } else {
                    output { showEffect(Effect.RestartProcess) }
                }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Error.whenError(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(State.Loading) {
                input { showState(this.state) }
                input { selectPaymentOptionUseCase.select() }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun Action.LoadContractSuccess.whenContractSuccess(): Out<State, Action> {
        val content = State.Content(
            shopTitle = paymentParameters.title,
            shopSubtitle = paymentParameters.subtitle,
            paymentOption = outputModel.paymentOption,
            savePaymentMethod = when (paymentParameters.savePaymentMethod) {
                SavePaymentMethod.ON -> true
                else -> false
            },
            showAllowWalletLinking = outputModel.walletLinkingPossible,
            allowWalletLinking = true,
            confirmation = getConfirmation(outputModel.paymentOption)
        )
        return when (outputModel.paymentOption) {
            is GooglePay -> {
                val fee = outputModel.paymentOption.fee?.service
                val savePaymentMethodAllowed = outputModel.paymentOption.savePaymentMethodAllowed
                if((savePaymentMethodAllowed && paymentParameters.savePaymentMethod != SavePaymentMethod.OFF) || !fee.isNullOrZero()) {
                    Out(content) {
                        input { showState(this.state) }
                    }
                } else {
                    Out(State.GooglePay(content, outputModel.paymentOption.id)) {
                        input{ showState(this.state)}
                    }
                }
            }
            else -> Out(content) {
                input { showState(this.state) }
            }
        }
    }
}

private fun State.Content.getTokenizeInputModel(paymentOptionInfo: PaymentOptionInfo?): TokenizeInputModel {
    return TokenizeInputModel(
        paymentOptionId =  paymentOption.id,
        savePaymentMethod = savePaymentMethod,
        confirmation = confirmation,
        paymentOptionInfo = paymentOptionInfo,
        allowWalletLinking = allowWalletLinking
    )
}