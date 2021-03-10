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

package ru.yoo.sdk.kassa.payments.extensions

import android.content.Context
import android.graphics.drawable.InsetDrawable
import androidx.appcompat.content.res.AppCompatResources
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeBankCard
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeGooglePay
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeLinkedCard
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeRecurring
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeSbolSms
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeWallet
import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.Confirmation
import ru.yoo.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.Wallet
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.utils.UNKNOWN_CARD_ICON
import ru.yoo.sdk.kassa.payments.utils.getBankLogo

internal fun PaymentOption.getIcon(context: Context) = checkNotNull(
    when (this) {
            is NewCard, is PaymentIdCscConfirmation -> {
                InsetDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ym_ic_add_card),
                    context.resources.getDimensionPixelSize(R.dimen.ym_space2XS)
                )
            }
            is Wallet, is AbstractWallet -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_yoomoney)
            is LinkedCard ->  {
                val logo = getBankLogo(pan).let {
                    it.takeIf { it != UNKNOWN_CARD_ICON} ?: brand.getIconResId()
                }
                InsetDrawable(
                    AppCompatResources.getDrawable(context, logo),
                    context.resources.getDimensionPixelSize(R.dimen.ym_space2XS)
                )
            }
            is SbolSmsInvoicing -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_sberbank)
            is GooglePay -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_google_pay)
    }
) { "icon not found for $this" }

internal fun PaymentOption.getTitle(context: Context): CharSequence = when (this) {
    is NewCard -> context.getText(R.string.ym_payment_option_new_card)
    is Wallet -> context.getText(R.string.ym_payment_option_yoomoney)
    is AbstractWallet -> context.getText(R.string.ym_payment_option_yoomoney)
    is LinkedCard -> if (isLinkedToWallet) {
        name.takeUnless(String?::isNullOrEmpty) ?: context.getString(R.string.ym_linked_card)
    } else {
        "•••• " + pan.takeLast(4)
    }
    is SbolSmsInvoicing -> context.getText(R.string.ym_sberbank)
    is GooglePay -> context.getText(R.string.ym_payment_option_google_pay)
    is PaymentIdCscConfirmation -> context.getText(R.string.ym_saved_card)
}

internal fun PaymentOption.getAdditionalInfo(): CharSequence? {
    return when(this) {
        is Wallet -> balance.format()
        is LinkedCard ->  ("•••• " + pan.takeLast(4)).takeIf { isLinkedToWallet }
        else -> null
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
