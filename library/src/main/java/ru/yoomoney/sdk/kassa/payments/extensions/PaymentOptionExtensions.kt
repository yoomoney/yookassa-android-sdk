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

package ru.yoomoney.sdk.kassa.payments.extensions

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooImage
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeBankCard
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeGooglePay
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeLinkedCard
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeLinkedToShopCard
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeLinkedToShopCardWithCvc
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeRecurring
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeSberPay
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeSbolSms
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeWallet
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.ui.view.cropToCircle
import ru.yoomoney.sdk.kassa.payments.utils.INVOICING_AUTHORITY
import ru.yoomoney.sdk.kassa.payments.utils.SBERPAY_PATH
import ru.yoomoney.sdk.kassa.payments.utils.getBankOrBrandLogo

internal fun PaymentOption.getPlaceholderIcon(context: Context) = checkNotNull(
    when (this) {
        is BankCardPaymentOption, is PaymentIdCscConfirmation -> {
            AppCompatResources.getDrawable(context, R.drawable.ym_ic_add_card)
        }
        is Wallet, is AbstractWallet -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_yoomoney)
        is LinkedCard -> {
            val logo = getBankOrBrandLogo(pan, brand)
            InsetDrawable(
                AppCompatResources.getDrawable(context, logo),
                context.resources.getDimensionPixelSize(R.dimen.ym_space2XS)
            )
        }
        is SberBank -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_sberbank)
        is GooglePay -> AppCompatResources.getDrawable(context, R.drawable.ym_ic_google_pay)
    }
) { "icon not found for $this" }

internal fun PaymentOption.getPlaceholderTitle(context: Context): CharSequence = when (this) {
    is BankCardPaymentOption -> context.getText(R.string.ym_payment_option_new_card)
    is Wallet -> context.getText(R.string.ym_payment_option_yoomoney)
    is AbstractWallet -> context.getText(R.string.ym_payment_option_yoomoney)
    is LinkedCard -> name.takeUnless(String?::isNullOrEmpty) ?: "•••• " + pan.takeLast(4)
    is SberBank -> context.getText(R.string.ym_sberbank)
    is GooglePay -> context.getText(R.string.ym_payment_option_google_pay)
    is PaymentIdCscConfirmation -> context.getText(R.string.ym_saved_card)
}

internal fun PaymentOption.getAdditionalInfo(context: Context): CharSequence? {
    return when (this) {
        is Wallet -> balance.format()
        is LinkedCard -> context.getString(R.string.ym_linked_wallet_card)
        else -> null
    }
}

internal fun PaymentOption.toTokenizeScheme(
    context: Context,
    sberbankPackage: String,
    paymentInstrumentBankCard: PaymentInstrumentBankCard?
) = when (this) {
    is Wallet, is AbstractWallet -> TokenizeSchemeWallet()
    is LinkedCard -> TokenizeSchemeLinkedCard()
    is BankCardPaymentOption -> getTokenizeSchemeLinkedCard(paymentInstrumentBankCard)
    is SberBank -> if (canPayWithSberPay(context, sberbankPackage)) {
        TokenizeSchemeSberPay()
    } else {
        TokenizeSchemeSbolSms()
    }
    is GooglePay -> TokenizeSchemeGooglePay()
    is PaymentIdCscConfirmation -> TokenizeSchemeRecurring()
}

private fun getTokenizeSchemeLinkedCard(paymentInstrumentBankCard: PaymentInstrumentBankCard?) = when {
    paymentInstrumentBankCard != null && paymentInstrumentBankCard.cscRequired -> TokenizeSchemeLinkedToShopCardWithCvc()
    paymentInstrumentBankCard != null -> TokenizeSchemeLinkedToShopCard()
    else -> TokenizeSchemeBankCard()
}

internal fun PaymentOption.getConfirmation(
    context: Context,
    returnUrl: String,
    appScheme: String,
    sberbankPackage: String
): Confirmation {
    return when (this) {
        is YooMoney, is BankCardPaymentOption, is GooglePay, is PaymentIdCscConfirmation -> RedirectConfirmation(
            returnUrl
        )
        is SberBank -> if (canPayWithSberPay(context, sberbankPackage)) {
            MobileApplication("$appScheme://$INVOICING_AUTHORITY/$SBERPAY_PATH")
        } else {
            ExternalConfirmation
        }
    }
}
