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

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.SavePaymentMethodOptionTexts
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel

internal sealed class ContractInfo {
    abstract val paymentOption: PaymentOption

    data class WalletContractInfo(
        override val paymentOption: Wallet,
        val walletUserAuthName: String?,
        val walletUserAvatarUrl: String?,
        val showAllowWalletLinking: Boolean,
        val allowWalletLinking: Boolean
    ): ContractInfo()

    data class WalletLinkedCardContractInfo(
        override val paymentOption: LinkedCard,
        val showAllowWalletLinking: Boolean,
        val allowWalletLinking: Boolean
    ): ContractInfo()

    data class PaymentIdCscConfirmationContractInfo(
        override val paymentOption: PaymentIdCscConfirmation,
        val allowWalletLinking: Boolean
    ): ContractInfo()

    data class NewBankCardContractInfo(
        override val paymentOption: BankCardPaymentOption
    ): ContractInfo()

    data class LinkedBankCardContractInfo(
        override val paymentOption: BankCardPaymentOption,
        val instrument: PaymentInstrumentBankCard
    ): ContractInfo()

    data class AbstractWalletContractInfo(
        override val paymentOption: AbstractWallet
    ): ContractInfo()

    data class GooglePayContractInfo(
        override val paymentOption: GooglePay
    ): ContractInfo()

    data class SberBankContractInfo(
        override val paymentOption: SberBank,
        val userPhoneNumber: String?
    ): ContractInfo()
}

internal object Contract {

    sealed class State {
        object Loading : State() {
            override fun toString() = "State.Loading"
        }

        data class Error(val error: Throwable) : State()

        data class Content(
            val shopTitle: CharSequence,
            val shopSubtitle: CharSequence,
            val isSinglePaymentMethod: Boolean,
            val shouldSavePaymentMethod: Boolean,
            val shouldSavePaymentInstrument: Boolean,
            val savePaymentMethod: SavePaymentMethod,
            val contractInfo: ContractInfo,
            val confirmation: Confirmation,
            val isSplitPayment: Boolean,
            val customerId: String?,
            val savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
            val userAgreementUrl: String
        ) : State()

        data class GooglePay(
            val content: Content,
            val paymentOptionId: Int
        ) : State()
    }

    sealed class Action {
        object Load : Action()
        data class LoadContractFailed(val error: Throwable) : Action()
        data class LoadContractSuccess(val outputModel: SelectedPaymentMethodOutputModel) : Action()

        data class Tokenize(val paymentOptionInfo: PaymentOptionInfo? = null): Action()
        data class TokenizePaymentInstrument(val instrument: PaymentInstrumentBankCard, val csc: String?): Action()
        object TokenizeCancelled: Action() {
            override fun toString() = "Action.TokenizeCancelled"
        }

        object RestartProcess: Action() {
            override fun toString() = "Action.RestartProcess"
        }
        data class ChangeSavePaymentMethod(val savePaymentMethod: Boolean): Action()
        data class ChangeAllowWalletLinking(val isAllowed: Boolean): Action()

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
        data class ShowTokenize(val tokenizeInputModel: TokenizeInputModel): Effect() {
            override fun toString() = "Effect.ShowTokenize"
        }
        data class StartGooglePay(
            val content: State.Content,
            val paymentOptionId: Int
        ) : Effect()
    }
}