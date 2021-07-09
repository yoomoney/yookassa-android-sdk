/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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
import android.content.Intent
import androidx.core.content.ContextCompat
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.utils.getMessageWithLink

internal class ContractFormatter {

    companion object {

        internal fun getSwitchOnSavePaymentMethodTitle(context: Context, paymentOption: PaymentOption): CharSequence {
            return if (paymentOption is Wallet || paymentOption is AbstractWallet) {
                context.getString(R.string.ym_contract_save_payment_method_wallet_title)
            } else {
                context.getString(R.string.ym_save_payment_method_title)
            }
        }

        internal fun getUserSelectsSavePaymentMethodTitle(context: Context, paymentOption: PaymentOption): CharSequence {
            return if (paymentOption is Wallet || paymentOption is AbstractWallet) {
                context.getString(R.string.ym_contract_save_payment_method_wallet_switch_title)
            } else if (paymentOption is GooglePay) {
                context.getString(R.string.ym_contract_save_payment_method_gpay_switch_title)
            } else {
                context.getString(R.string.ym_contract_link_card_title)
            }
        }

        internal fun getSavePaymentMethodMessageLink(context: Context, paymentOption: PaymentOption): CharSequence {
            val linkTextFirstPart: Int
            val linkTextSecondPart: Int
            if (paymentOption is Wallet || paymentOption is AbstractWallet) {
                linkTextFirstPart = R.string.ym_save_payment_method_wallet_message_text_part_1
                linkTextSecondPart = R.string.ym_save_payment_method_wallet_message_text_part_2
            } else {
                linkTextFirstPart = R.string.ym_save_payment_method_bank_card_message_text_part_1
                linkTextSecondPart = R.string.ym_save_payment_method_bank_card_message_text_part_2
            }

            return getLinkForSavePaymentMethodScreen(
                context = context,
                linkTextFirstPart = linkTextFirstPart,
                linkTextSecondPart = linkTextSecondPart,
                paymentOption = paymentOption
            )
        }

        internal fun getSavePaymentMethodSwitchLink(context: Context, paymentOption: PaymentOption): CharSequence {
            val linkTextFirstPart: Int
            val linkTextSecondPart: Int
            if (paymentOption is Wallet || paymentOption is AbstractWallet) {
                linkTextFirstPart = R.string.ym_save_payment_method_wallet_switch_text_part_1
                linkTextSecondPart = R.string.ym_save_payment_method_wallet_switch_text_part_2
            } else if(paymentOption is GooglePay) {
                linkTextFirstPart = R.string.ym_save_payment_method_gpay_switch_text_part_1
                linkTextSecondPart = R.string.ym_save_payment_method_gpay_switch_text_part_2
            }else {
                linkTextFirstPart = R.string.ym_save_payment_method_bank_card_switch_text_part_1
                linkTextSecondPart = R.string.ym_save_payment_method_bank_card_switch_text_part_2
            }

            return getLinkForSavePaymentMethodScreen(
                context = context,
                linkTextFirstPart = linkTextFirstPart,
                linkTextSecondPart = linkTextSecondPart,
                paymentOption = paymentOption
            )
        }

        private fun getLinkForSavePaymentMethodScreen(
            context: Context,
            linkTextFirstPart: Int,
            linkTextSecondPart: Int,
            paymentOption: PaymentOption
        ): CharSequence {

            val screenTitle: Int
            val screenText: Int
            var additionalInfo: Int? = null

            if (paymentOption is Wallet || paymentOption is AbstractWallet) {
                screenTitle = R.string.ym_save_payment_method_wallet_info_title
                screenText = R.string.ym_save_payment_method_wallet_info_text
            } else {
                screenTitle = R.string.ym_save_payment_method_bank_card_info_title
                screenText = R.string.ym_save_payment_method_bank_card_info_text
                additionalInfo = R.string.ym_save_payment_method_bank_card_additional_info
            }

            return getMessageWithLink(
                context,
                linkTextFirstPart,
                linkTextSecondPart
            ) {
                ContextCompat.startActivity(
                    context, SavePaymentMethodInfoActivity.create(context, screenTitle, screenText, additionalInfo)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
                )
            }
        }
    }
}
