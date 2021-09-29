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
import ru.yoomoney.sdk.kassa.payments.model.SavePaymentMethodOptionTexts

internal sealed class SavePaymentMethodOption {

    data class SwitchSavePaymentMethodOption(
        val title: String,
        val subtitle: String,
        val screenTitle: String,
        val screenText: String
    ): SavePaymentMethodOption()

    data class MessageSavePaymentMethodOption(
        val title: String,
        val subtitle: String,
        val screenTitle: String,
        val screenText: String
    ): SavePaymentMethodOption()

    object None: SavePaymentMethodOption()
}

internal fun Contract.State.Content.getSavePaymentMethodOption(): SavePaymentMethodOption {
    val isBindAllowed = contractInfo.paymentOption.savePaymentInstrument && !customerId.isNullOrBlank()
    val isRecurrentAllowed = contractInfo.paymentOption.savePaymentMethodAllowed
    return when(contractInfo) {
        is ContractInfo.WalletContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.WalletLinkedCardContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.AbstractWalletContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.SberBankContractInfo -> getSberPaySavePaymentOption(
            savePaymentMethod = savePaymentMethod,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            isRecurrentAllowed = isRecurrentAllowed
        )
        is ContractInfo.LinkedBankCardContractInfo -> getLinkedBankCardSavePaymentOption(
            savePaymentMethod = savePaymentMethod,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            isRecurrentAllowed = isRecurrentAllowed
        )
        is ContractInfo.PaymentIdCscConfirmationContractInfo -> SavePaymentMethodOption.None
        is ContractInfo.NewBankCardContractInfo -> getNewBankCardSavePaymentOption(
            savePaymentMethod = savePaymentMethod,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            isBindAllowed = isBindAllowed,
            isRecurrentAllowed = isRecurrentAllowed
        )
        is ContractInfo.GooglePayContractInfo -> getGooglePaySavePaymentOption(
            savePaymentMethod = savePaymentMethod,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            isRecurrentAllowed = isRecurrentAllowed
        )
    }
}

private fun getLinkedBankCardSavePaymentOption(
    savePaymentMethod: SavePaymentMethod,
    savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOnBindOffTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOnBindOffSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.OFF -> SavePaymentMethodOption.None
        SavePaymentMethod.USER_SELECTS -> when {
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = savePaymentMethodOptionTexts.switchRecurrentOnBindOffTitle,
                    subtitle = savePaymentMethodOptionTexts.switchRecurrentOnBindOffSubtitle,
                    screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                    screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
                )
            else -> SavePaymentMethodOption.None
        }
    }
}

private fun getNewBankCardSavePaymentOption(
    savePaymentMethod: SavePaymentMethod,
    savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    isBindAllowed: Boolean,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod) {
        SavePaymentMethod.ON -> when {
            isBindAllowed && isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOnBindOnTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOnBindOnSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOnTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOnText
            )
            isBindAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOffBindOnTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOffBindOnSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOffBindOnTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOffBindOnText
            )
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOnBindOffTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOnBindOffSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.OFF -> when {
            isBindAllowed -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.switchRecurrentOffBindOnTitle,
                subtitle = savePaymentMethodOptionTexts.switchRecurrentOffBindOnSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOffBindOnTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOffBindOnText
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.USER_SELECTS -> when {
            isBindAllowed && isRecurrentAllowed -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.switchRecurrentOnBindOnTitle,
                subtitle = savePaymentMethodOptionTexts.switchRecurrentOnBindOnSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOnTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOnText
            )
            isBindAllowed ->  SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.switchRecurrentOffBindOnTitle,
                subtitle = savePaymentMethodOptionTexts.switchRecurrentOffBindOnSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOffBindOnTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOffBindOnText
            )
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = savePaymentMethodOptionTexts.switchRecurrentOnBindOffTitle,
                    subtitle = savePaymentMethodOptionTexts.switchRecurrentOnBindOffSubtitle,
                    screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                    screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
                )
            else -> SavePaymentMethodOption.None
        }
    }
}

private fun getGooglePaySavePaymentOption(
    savePaymentMethod: SavePaymentMethod,
    savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOnBindOffTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOnBindOffSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.USER_SELECTS -> when {
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = savePaymentMethodOptionTexts.switchRecurrentOnBindOffTitle,
                    subtitle = savePaymentMethodOptionTexts.switchRecurrentOnBindOffSubtitle,
                    screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                    screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
                )
            else -> SavePaymentMethodOption.None
        }
        else -> SavePaymentMethodOption.None
    }
}

private fun getSberPaySavePaymentOption(
    savePaymentMethod: SavePaymentMethod,
    savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    isRecurrentAllowed: Boolean
): SavePaymentMethodOption {
    return when(savePaymentMethod){
        SavePaymentMethod.ON -> when {
            isRecurrentAllowed -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = savePaymentMethodOptionTexts.messageRecurrentOnBindOffTitle,
                subtitle = savePaymentMethodOptionTexts.messageRecurrentOnBindOffSubtitle,
                screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnSberpayTitle,
                screenText = savePaymentMethodOptionTexts.screenRecurrentOnSberpayText
            )
            else -> SavePaymentMethodOption.None
        }
        SavePaymentMethod.USER_SELECTS -> when {
            isRecurrentAllowed ->
                SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                    title = savePaymentMethodOptionTexts.switchRecurrentOnBindOffTitle,
                    subtitle = savePaymentMethodOptionTexts.switchRecurrentOnBindOffSubtitle,
                    screenTitle = savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle,
                    screenText = savePaymentMethodOptionTexts.screenRecurrentOnBindOffText
                )
            else -> SavePaymentMethodOption.None
        }
        else -> SavePaymentMethodOption.None
    }
}