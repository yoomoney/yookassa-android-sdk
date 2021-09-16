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
import ru.yoomoney.sdk.kassa.payments.utils.getMessageWithLink

internal class SavePaymentOptionFormatter {

    companion object {

        internal fun getNewBankCardSwitchRecurrentBindSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_with_switch_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_with_switch_part_2
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_auto_write_off_and_save_card_title,
                screenText = R.string.ym_auto_write_off_and_save_card_body
            )
        }

        internal fun getNewBankCardSwitchBindSubtitle(context: Context): CharSequence = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_part_2,
            thirdMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_part_3
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_save_card_title,
                screenText = R.string.ym_save_card_body
            )
        }

        internal fun getNewBankCardSwitchRecurrentSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_card_message_text_with_switch_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_card_message_text_with_switch_part_2
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_how_works_auto_write_title,
                screenText = R.string.ym_how_works_auto_write_body
            )
        }

        internal fun getNewBankCardMessageRecurrentBindSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_part_2
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_auto_write_off_and_save_card_title,
                screenText = R.string.ym_auto_write_off_and_save_card_body
            )
        }

        internal fun getNewBankCardMessageRecurrentSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_card_message_text_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_card_message_text_part_2
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_how_works_auto_write_title,
                screenText = R.string.ym_how_works_auto_write_body
            )
        }

        internal fun getNewBankCardBindMessageSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_2,
            thirdMessagePartRes = R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_3
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_save_card_title,
                screenText = R.string.ym_save_card_body
            )
        }

        internal fun getGpayRecurrentMessageSubtitle(context: Context) = getMessageWithLink(
            context = context,
            firstMessagePartRes = R.string.ym_save_payment_method_gpay_switch_text_part_1,
            secondMessagePartRes = R.string.ym_save_payment_method_gpay_switch_text_part_2
        ) {
            startInfo(
                context = context,
                screenTitle = R.string.ym_save_payment_method_bank_card_info_title,
                screenText = R.string.ym_save_payment_method_bank_card_info_text,
                additionalInfo = R.string.ym_save_payment_method_bank_card_additional_info
            )
        }

        private fun startInfo(context: Context, screenTitle: Int, screenText: Int, additionalInfo: Int? = null) {
            ContextCompat.startActivity(
                context,
                SavePaymentMethodInfoActivity.create(context, screenTitle, screenText, additionalInfo)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
            )
        }

    }
}
