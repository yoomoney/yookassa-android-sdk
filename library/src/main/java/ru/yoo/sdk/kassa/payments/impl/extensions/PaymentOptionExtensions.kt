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

@file:JvmName("PaymentOptionExtensions")

package ru.yoo.sdk.kassa.payments.impl.extensions

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import androidx.appcompat.content.res.AppCompatResources
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeBankCard
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeGooglePay
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeLinkedCard
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeRecurring
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeSbolSms
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeWallet
import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.Confirmation
import ru.yoo.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.Wallet
import ru.yoo.sdk.kassa.payments.model.YooMoney

internal fun PaymentOption.getIcon(context: Context) = checkNotNull(
    AppCompatResources.getDrawable(
        context, when (this) {
            is NewCard -> R.drawable.ym_ic_add_card
            is Wallet, is AbstractWallet -> R.drawable.ym_ic_yoomoney
            is LinkedCard -> brand.getIconResId()
            is SbolSmsInvoicing -> R.drawable.ym_ic_sberbank
            is GooglePay -> R.drawable.ym_ic_google_pay
            is PaymentIdCscConfirmation -> R.drawable.ym_ic_add_card
        }
    )?.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
) { "icon not found for $this" }

internal fun PaymentOption.getTitle(context: Context): CharSequence = when (this) {
    is NewCard -> context.getText(R.string.ym_payment_option_new_card)
    is Wallet -> walletId
    is AbstractWallet -> context.getText(R.string.ym_payment_option_yoomoney)
    is LinkedCard -> name.takeUnless(String?::isNullOrEmpty) ?: pan.chunked(4).joinToString(" ")
    is SbolSmsInvoicing -> context.getText(R.string.ym_sberbank)
    is GooglePay -> context.getText(R.string.ym_payment_option_google_pay)
    is PaymentIdCscConfirmation -> context.getText(R.string.ym_payment_option_new_card)
}.let { title ->
    title.takeIf { this is Wallet }?.let {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ym_ic_exit)?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        val imageSpan = ImageSpan(requireNotNull(drawable), ImageSpan.ALIGN_BASELINE)
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.ym_button_text_link))
        SpannableStringBuilder.valueOf(it).append("   ").apply {
            setSpan(imageSpan, lastIndex - 1, lastIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            setSpan(colorSpan, 0, lastIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
    } ?: title
}

internal fun PaymentOption.getAdditionalInfo() = (this as? Wallet)?.balance?.let {
    SpannableStringBuilder.valueOf(it.format()).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

internal fun PaymentOption.toTokenizeScheme() = when (this) {
    is Wallet, is AbstractWallet -> TokenizeSchemeWallet()
    is LinkedCard -> TokenizeSchemeLinkedCard()
    is NewCard -> TokenizeSchemeBankCard()
    is SbolSmsInvoicing -> TokenizeSchemeSbolSms()
    is GooglePay -> TokenizeSchemeGooglePay()
    is PaymentIdCscConfirmation -> TokenizeSchemeRecurring()
}

internal fun PaymentOption.getConfirmation(returnUrl: String): Confirmation {
    return when (this) {
        is YooMoney, is NewCard, is GooglePay, is PaymentIdCscConfirmation -> RedirectConfirmation(
            returnUrl
        )
        is SbolSmsInvoicing -> ExternalConfirmation
    }
}
