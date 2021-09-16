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
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInstrumentInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInputModel
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output
import ru.yoomoney.sdk.kassa.payments.model.isSplitPayment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository

internal class ContractBusinessLogic(
    val showState: suspend (State) -> Action,
    val showEffect: suspend (Effect) -> Unit,
    val source: suspend () -> Action,
    private val paymentParameters: PaymentParameters,
    private val selectPaymentMethodUseCase: SelectPaymentMethodUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getConfirmation: GetConfirmation,
    private val loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val shopPropertiesRepository: ShopPropertiesRepository
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.Content -> state.whenContent(action)
        is State.GooglePay -> state.whenGooglePay(action)
        is State.Error -> state.whenError(action)
    }

    private fun State.Loading.whenLoading(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Load -> Out(this) {
                input { selectPaymentMethodUseCase.select() }
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
                input { selectPaymentMethodUseCase.select() }
            }
            is Action.Tokenize -> Out(this) {
                input { showState(this.state) }
                output {
                    showEffect(Effect.ShowTokenize(this.state.getTokenizeInputModel(action.paymentOptionInfo)))
                }
            }
            is Action.TokenizePaymentInstrument -> Out(this) {
                input { showState(this.state) }
                output {
                    showEffect(
                        Effect.ShowTokenize(
                            this.state.getTokenizeInputModel(
                                requireNotNull(action.instrument),
                                action.csc
                            )
                        )
                    )
                }
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
            is Action.ChangeAllowWalletLinking -> when (contractInfo) {
                is ContractInfo.WalletContractInfo -> Out(this.copy(contractInfo = contractInfo.copy(allowWalletLinking = action.isAllowed))) {
                    input(source)
                }
                else -> Out.skip(this, source)
            }
            is Action.ChangeSavePaymentMethod -> Out(
                this.copy(
                    shouldSavePaymentMethod = action.savePaymentMethod && this.contractInfo.paymentOption.savePaymentMethodAllowed,
                    shouldSavePaymentInstrument = action.savePaymentMethod && this.contractInfo.paymentOption.savePaymentInstrument
                )
            ) {
                input(source)
            }
            is Action.GooglePayCancelled -> Out(State.Loading) {
                input { selectPaymentMethodUseCase.select() }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.GooglePay.whenGooglePay(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Tokenize -> Out(this) {
                input(source)
                output {
                    showEffect(Effect.ShowTokenize(this.state.content.getTokenizeInputModel(action.paymentOptionInfo)))
                }
            }
            is Action.GooglePayCancelled -> Out(this) {
                if (paymentParameters.paymentMethodTypes.size == 1) {
                    output { showEffect(Effect.CancelProcess) }
                } else {
                    output { showEffect(Effect.RestartProcess) }
                }
            }
            is Action.TokenizeCancelled -> Out(this) {
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
                input { selectPaymentMethodUseCase.select() }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun Action.LoadContractSuccess.whenContractSuccess(): Out<State, Action> {
        val isSplitPayment = shopPropertiesRepository.shopProperties.isSplitPayment
        val shouldSavePaymentMethod = when (paymentParameters.savePaymentMethod) {
            SavePaymentMethod.ON -> true
            else -> false
        }

        val contractInfo = outputModel.toContractInfo()
        val shouldSavePaymentInstrument = when (contractInfo) {
            is ContractInfo.NewBankCardContractInfo -> when (paymentParameters.savePaymentMethod) {
                SavePaymentMethod.OFF -> outputModel.paymentOption.savePaymentInstrument
                SavePaymentMethod.ON -> shouldSavePaymentMethod && outputModel.paymentOption.savePaymentInstrument
                SavePaymentMethod.USER_SELECTS -> {
                    !outputModel.paymentOption.savePaymentMethodAllowed && outputModel.paymentOption.savePaymentInstrument
                }
            }
            else -> false
        }

        val loadedPaymentOptions = loadedPaymentOptionListRepository.getLoadedPaymentOptions()
        var paymentMethodsCount = loadedPaymentOptions.size
        loadedPaymentOptions.filterIsInstance(BankCardPaymentOption::class.java).forEach {
            paymentMethodsCount += it.paymentInstruments.size
        }

        val content = State.Content(
            shopTitle = paymentParameters.title,
            shopSubtitle = paymentParameters.subtitle,
            isSinglePaymentMethod = paymentMethodsCount == 1,
            contractInfo = contractInfo,
            shouldSavePaymentMethod = shouldSavePaymentMethod && outputModel.paymentOption.savePaymentMethodAllowed,
            shouldSavePaymentInstrument = shouldSavePaymentInstrument,
            savePaymentMethod = paymentParameters.savePaymentMethod,
            confirmation = getConfirmation(outputModel.paymentOption),
            isSplitPayment = isSplitPayment,
            customerId = paymentParameters.customerId
        )
        return when (outputModel.paymentOption) {
            is GooglePay -> {
                val fee = outputModel.paymentOption.fee?.service
                val savePaymentMethodAllowed = outputModel.paymentOption.savePaymentMethodAllowed
                if((savePaymentMethodAllowed && paymentParameters.savePaymentMethod != SavePaymentMethod.OFF) || !fee.isNullOrZero() || isSplitPayment) {
                    Out(content) {
                        input { showState(this.state) }
                    }
                } else {
                    Out(State.GooglePay(content, outputModel.paymentOption.id)) {
                        input { showState(this.state) }
                        output { showEffect(Effect.StartGooglePay(content, outputModel.paymentOption.id)) }
                    }
                }
            }
            else -> Out(content) {
                input { showState(this.state) }
            }
        }
    }

    private fun SelectedPaymentMethodOutputModel.toContractInfo(): ContractInfo {
        return when (paymentOption) {
            is Wallet -> ContractInfo.WalletContractInfo(
                paymentOption = paymentOption,
                walletUserAuthName = userAuthInfoRepository.userAuthName,
                walletUserAvatarUrl = userAuthInfoRepository.userAvatarUrl,
                showAllowWalletLinking = walletLinkingPossible,
                allowWalletLinking = true
            )
            is LinkedCard -> ContractInfo.WalletLinkedCardContractInfo(
                paymentOption = paymentOption,
                showAllowWalletLinking = walletLinkingPossible,
                allowWalletLinking = true
            )
            is PaymentIdCscConfirmation -> ContractInfo.PaymentIdCscConfirmationContractInfo(
                paymentOption = paymentOption,
                allowWalletLinking = true
            )
            is BankCardPaymentOption -> {
                if (instrument != null) {
                    ContractInfo.LinkedBankCardContractInfo(paymentOption, instrument)
                } else {
                    ContractInfo.NewBankCardContractInfo(paymentOption)
                }
            }
            is GooglePay -> ContractInfo.GooglePayContractInfo(paymentOption)
            is SberBank -> ContractInfo.SberBankContractInfo(paymentOption, paymentParameters.userPhoneNumber)
            is AbstractWallet -> ContractInfo.AbstractWalletContractInfo(paymentOption)
        }
    }
}

private fun State.Content.getTokenizeInputModel(paymentOptionInfo: PaymentOptionInfo?): TokenizeInputModel {
    return TokenizePaymentOptionInputModel(
        paymentOptionId = contractInfo.paymentOption.id,
        savePaymentMethod = shouldSavePaymentMethod,
        savePaymentInstrument = shouldSavePaymentInstrument,
        confirmation = confirmation,
        paymentOptionInfo = paymentOptionInfo,
        allowWalletLinking = (contractInfo as? ContractInfo.WalletContractInfo)?.allowWalletLinking ?: false
    )
}

private fun State.Content.getTokenizeInputModel(
    instrumentBankCard: PaymentInstrumentBankCard,
    csc: String?
): TokenizeInputModel {
    return TokenizeInstrumentInputModel(
        paymentOptionId = contractInfo.paymentOption.id,
        instrumentBankCard = instrumentBankCard,
        savePaymentMethod = shouldSavePaymentMethod,
        allowWalletLinking = (contractInfo as? ContractInfo.WalletContractInfo)?.allowWalletLinking ?: false,
        confirmation = confirmation,
        csc = csc
    )
}