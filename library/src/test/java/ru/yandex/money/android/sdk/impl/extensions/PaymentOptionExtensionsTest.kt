/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.impl.extensions

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.createAbstractWalletPaymentOption
import ru.yandex.money.android.sdk.createGooglePayPaymentOptionWithFee
import ru.yandex.money.android.sdk.createLinkedCardPaymentOption
import ru.yandex.money.android.sdk.createNewCardPaymentOption
import ru.yandex.money.android.sdk.createSbolSmsInvoicingPaymentOption
import ru.yandex.money.android.sdk.createWalletPaymentOption
import ru.yandex.money.android.sdk.equalToDrawable
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeBankCard
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeGooglePay
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeLinkedCard
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeSbolSms
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeWallet
import ru.yandex.money.android.sdk.model.ExternalConfirmation
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.NoConfirmation
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.RedirectConfirmation
import ru.yandex.money.android.sdk.model.Wallet
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PaymentOptionExtensionsTest {

    @[Rule JvmField]
    val x = Timeout(1, TimeUnit.MINUTES)

    private val context = RuntimeEnvironment.application

    @Test
    fun `get icon for Wallet`() {
        // prepare
        val paymentOption = createWalletPaymentOption(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon, equalToDrawable(AppCompatResources.getDrawable(context, R.drawable.ym_ic_yamoney)))
    }

    @Test
    fun `get icon for AbstractWallet`() {
        // prepare
        val paymentOption = createAbstractWalletPaymentOption(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon, equalToDrawable(AppCompatResources.getDrawable(context, R.drawable.ym_ic_yamoney)))
    }

    @Test
    fun `get icon for LinkedCard`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1) as LinkedCard

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon, equalToDrawable(AppCompatResources.getDrawable(context, paymentOption.brand.getIconResId())))
    }

    @Test
    fun `get icon for BankCard`() {
        // prepare
        val paymentOption = createNewCardPaymentOption(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon, equalToDrawable(AppCompatResources.getDrawable(context, R.drawable.ym_ic_add_card)))
    }

    @Test
    fun `get icon for GooglePay`() {
        // prepare
        val paymentOption = createGooglePayPaymentOptionWithFee(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon, equalToDrawable(AppCompatResources.getDrawable(context, R.drawable.ym_ic_google_pay)))
    }

    @Test
    fun `get title for Wallet`() {
        // prepare
        val paymentOption = createWalletPaymentOption(1) as Wallet

        // invoke
        val title = paymentOption.getTitle(context) as SpannableStringBuilder

        // assert
        assertThat(title.toString(), equalTo("${paymentOption.userName}   "))

        val exitIcon = AppCompatResources.getDrawable(context, R.drawable.ym_ic_exit)
        val exitSpan = title.getSpans(title.length - 2, title.length - 1, ImageSpan::class.java).single().drawable
        assertThat(exitIcon, equalToDrawable(exitSpan))

        val colorValue = ContextCompat.getColor(context, R.color.ym_button_text_link)
        val colorSpan = title.getSpans(0, title.length - 1, ForegroundColorSpan::class.java).single().foregroundColor
        assertThat(colorSpan, equalTo(colorValue))
    }

    @Test
    fun `get title for AbstractWallet`() {
        // prepare
        val paymentOption = createAbstractWalletPaymentOption(1)

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title, equalTo(context.getText(R.string.ym_payment_option_yamoney)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard without name`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1) as LinkedCard

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title.toString(), equalTo(paymentOption.pan.chunked(4).joinToString(" ")))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard with empty name`() {
        // prepare
        val paymentOption = (createLinkedCardPaymentOption(1) as LinkedCard).copy(name = "")

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title.toString(), equalTo(paymentOption.pan.chunked(4).joinToString(" ")))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard with name`() {
        // prepare
        val paymentOption = (createLinkedCardPaymentOption(1) as LinkedCard).copy(name = "name")

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title.toString(), equalTo(paymentOption.name))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for NewCard`() {
        // prepare
        val paymentOption = createNewCardPaymentOption(1)

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title, equalTo(context.getText(R.string.ym_payment_option_new_card)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for GooglePay`() {
        // prepare
        val paymentOption = createGooglePayPaymentOptionWithFee(1)

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title, equalTo(context.getText(R.string.ym_payment_option_google_pay)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get additional info for Wallet`() {
        // prepare
        initExtensions(context)
        val paymentOption = createWalletPaymentOption(1) as Wallet

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo() as SpannableStringBuilder

        // assert
        assertThat(additionalInfo.toString(), equalTo(paymentOption.balance.format().toString()))

        val style = additionalInfo.getSpans(0, additionalInfo.length, StyleSpan::class.java).single().style
        assertThat(style, equalTo(Typeface.BOLD))
    }

    @Test
    fun `get additional info for AbstractWallet`() {
        // prepare
        val paymentOption = createAbstractWalletPaymentOption(1)

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo()

        // assert
        assertThat(additionalInfo, nullValue())
    }

    @Test
    fun `get additional info for LinkedCard`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1)

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo()

        // assert
        assertThat(additionalInfo, nullValue())
    }

    @Test
    fun `get additional info for BankCard`() {
        // prepare
        val paymentOption = createNewCardPaymentOption(1)

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo()

        // assert
        assertThat(additionalInfo, nullValue())
    }

    @Test
    fun `get additional info for GooglePay`() {
        // prepare
        val paymentOption = createGooglePayPaymentOptionWithFee(1)

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo()

        // assert
        assertThat(additionalInfo, nullValue())
    }

    @Test
    fun `test toTokenizeScheme()`() {
        // prepare
        val paymentOptions = listOf(
            createAbstractWalletPaymentOption(0),
            createWalletPaymentOption(1),
            createLinkedCardPaymentOption(2),
            createNewCardPaymentOption(3),
            createSbolSmsInvoicingPaymentOption(4),
            createGooglePayPaymentOptionWithFee(5)
        )
        val expectedTokenizeSchemes = arrayOf(
            TokenizeSchemeWallet(),
            TokenizeSchemeWallet(),
            TokenizeSchemeLinkedCard(),
            TokenizeSchemeBankCard(),
            TokenizeSchemeSbolSms(),
            TokenizeSchemeGooglePay()
        )

        // invoke
        val actualTokenizeSchemes = paymentOptions.map(PaymentOption::toTokenizeScheme)

        // assert
        assertThat(actualTokenizeSchemes, contains(*expectedTokenizeSchemes))
    }

    @Test
    fun `test toConfirmation`() {
        // prepare
        val redirectUrl = "test return url"
        val paymentOptions = listOf(
            createAbstractWalletPaymentOption(0),
            createWalletPaymentOption(1),
            createLinkedCardPaymentOption(2),
            createNewCardPaymentOption(3),
            createSbolSmsInvoicingPaymentOption(4),
            createGooglePayPaymentOptionWithFee(5)
        )
        val expectedConfirmations = arrayOf(
            RedirectConfirmation(redirectUrl),
            RedirectConfirmation(redirectUrl),
            RedirectConfirmation(redirectUrl),
            RedirectConfirmation(redirectUrl),
            ExternalConfirmation,
            RedirectConfirmation(redirectUrl)
        )

        // invoke
        val actualConfirmations = paymentOptions.map { it.toConfirmation(redirectUrl) }

        // assert
        assertThat(actualConfirmations, contains(*expectedConfirmations))
    }
}
