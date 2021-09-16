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

import android.content.Context
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getGpayRecurrentMessageSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardBindMessageSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardMessageRecurrentBindSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardMessageRecurrentSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardSwitchBindSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardSwitchRecurrentBindSubtitle
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentOptionFormatter.Companion.getNewBankCardSwitchRecurrentSubtitle

internal sealed class SavePaymentMethodOption {

    data class SwitchSavePaymentMethodOption(
        val title: CharSequence,
        val subtitle: CharSequence
    ): SavePaymentMethodOption()

    data class MessageSavePaymentMethodOption(
        val title: CharSequence,
        val subtitle: CharSequence
    ): SavePaymentMethodOption()

    object None: SavePaymentMethodOption()
}

internal fun Contract.State.Content.getSavePaymentMethodOption(context: Context): SavePaymentMethodOption {
    val isBindAllowed = contractInfo.paymentOption.savePaymentInstrument && !customerId.isNullOrBlank()
    val isRecurrentAllowed = contractInfo.paymentOption.savePaymentMethodAllowed
    return when(contractInfo) {
        is ContractInfo.WalletContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.WalletLinkedCardContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.AbstractWalletContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.SberBankContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.LinkedBankCardContractInfo -> getLinkedBankCardSavePaymentOption(
            context = context,
            savePaymentMethod = savePaymentMethod,
            isRecurrentAllowed = isRecurrentAllowed
        )
        is ContractInfo.PaymentIdCscConfirmationContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.NewBankCardContractInfo -> getNewBankCardSavePaymentOption(
            context = context,
            savePaymentMethod = savePaymentMethod,
            isBindAllowed = isBindAllowed,
            isRecurrentAllowed = isRecurrentAllowed
        )
        is ContractInfo.GooglePayContractInfo -> getGooglePaySavePaymentOption(
            context = context,
            savePaymentMethod = savePaymentMethod,
            isRecurrentAllowed = isRecurrentAllowed
        )
    }
}

private fun getLinkedBankCardSavePaymentOption(
    context: Context,
    savePaymentMethod: SavePaymentMethod,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_approve_without_switch),
                subtitle = getNewBankCardMessageRecurrentSubtitle(context)
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.OFF -> SavePaymentMethodOption.None
        SavePaymentMethod.USER_SELECTS -> when {
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = context.getString(R.string.ym_auto_write_off_approve_with_switch),
                    subtitle = getNewBankCardSwitchRecurrentSubtitle(context)
                )
            else -> SavePaymentMethodOption.None
        }
    }
}


private fun getNewBankCardSavePaymentOption(
    context: Context,
    savePaymentMethod: SavePaymentMethod,
    isBindAllowed: Boolean,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isBindAllowed && isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_save_payments_without_switch),
                subtitle = getNewBankCardMessageRecurrentBindSubtitle(context)
            )
            isBindAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_save_payment_details_without_switch),
                subtitle = getNewBankCardBindMessageSubtitle(context)
            )
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_approve_without_switch),
                subtitle = getNewBankCardMessageRecurrentSubtitle(context)
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.OFF -> when {
            isBindAllowed -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_save_payment_details_with_switch),
                subtitle = getNewBankCardSwitchBindSubtitle(context)
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.USER_SELECTS -> when {
            isBindAllowed && isRecurrentAllowed -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_save_payments_with_switch),
                subtitle = getNewBankCardSwitchRecurrentBindSubtitle(context)
            )
            isBindAllowed ->  SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_save_payment_details_with_switch),
                subtitle = getNewBankCardSwitchBindSubtitle(context)
            )
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = context.getString(R.string.ym_auto_write_off_approve_with_switch),
                    subtitle = getNewBankCardSwitchRecurrentSubtitle(context)
                )
            else -> SavePaymentMethodOption.None
        }
    }
}

private fun getGooglePaySavePaymentOption(
    context: Context,
    savePaymentMethod: SavePaymentMethod,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_contract_save_payment_method_gpay_switch_title),
                subtitle = getGpayRecurrentMessageSubtitle(context)
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.USER_SELECTS -> when {
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = context.getString(R.string.ym_contract_save_payment_method_gpay_switch_title),
                    subtitle = getGpayRecurrentMessageSubtitle(context)
                )
            else -> SavePaymentMethodOption.None
        }
        else -> SavePaymentMethodOption.None
    }
}