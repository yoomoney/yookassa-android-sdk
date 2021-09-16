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

import ru.yoomoney.sdk.kassa.payments.createAbstractWalletPaymentOption
import ru.yoomoney.sdk.kassa.payments.createBankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.createGooglePayPaymentOptionWithFee
import ru.yoomoney.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.createPaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoomoney.sdk.kassa.payments.createWalletPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet

internal fun createWalletContractInfo(wallet: Wallet = createWalletPaymentOption(1) as Wallet): ContractInfo.WalletContractInfo {
    return ContractInfo.WalletContractInfo(
        paymentOption = wallet,
        walletUserAuthName = "walletUserAuthName",
        walletUserAvatarUrl = "walletUserAvatarUrl",
        showAllowWalletLinking = true,
        allowWalletLinking = true
    )
}

internal fun createWalletLinkedCardContractInfo(option: LinkedCard = createLinkedCardPaymentOption(1) as LinkedCard): ContractInfo.WalletLinkedCardContractInfo {
    return ContractInfo.WalletLinkedCardContractInfo(
        paymentOption = option,
        showAllowWalletLinking = true,
        allowWalletLinking = true
    )
}

internal fun createAbstractWalletContractInfo(option: AbstractWallet = createAbstractWalletPaymentOption(1) as AbstractWallet): ContractInfo.AbstractWalletContractInfo {
    return ContractInfo.AbstractWalletContractInfo(paymentOption = option)
}

internal fun createSberBankContractInfo(
    option: SberBank = createSbolSmsInvoicingPaymentOption(1, isSberPayAllowed = false) as SberBank,
    userPhoneNumber: String? = null
): ContractInfo.SberBankContractInfo {
    return ContractInfo.SberBankContractInfo(option, userPhoneNumber)
}

internal fun createNewBankCardContractInfo(
    savePaymentMethodAllowed: Boolean = true,
    createBinding: Boolean = false,
    option: BankCardPaymentOption = createNewCardPaymentOption(1, savePaymentMethodAllowed, createBinding) as BankCardPaymentOption
): ContractInfo.NewBankCardContractInfo {
    return ContractInfo.NewBankCardContractInfo(paymentOption = option)
}

internal fun createLinkedBankCardContractInfo(
    instrument: PaymentInstrumentBankCard = createPaymentInstrumentBankCard(),
    option: BankCardPaymentOption = createBankCardPaymentOption(1, listOf(instrument)) as BankCardPaymentOption
): ContractInfo.LinkedBankCardContractInfo {
    return ContractInfo.LinkedBankCardContractInfo(paymentOption = option, instrument = instrument)
}

internal fun createGPayContractInfo(
    savePaymentMethodAllowed: Boolean,
    option: GooglePay = createGooglePayPaymentOptionWithFee(0, savePaymentMethodAllowed) as GooglePay
): ContractInfo.GooglePayContractInfo {
    return ContractInfo.GooglePayContractInfo(paymentOption = option)
}