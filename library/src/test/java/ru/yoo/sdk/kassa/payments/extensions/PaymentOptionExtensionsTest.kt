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

package ru.yoo.sdk.kassa.payments.extensions

import android.graphics.drawable.InsetDrawable
import android.text.SpannableStringBuilder
import androidx.appcompat.content.res.AppCompatResources
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
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.createAbstractWalletPaymentOption
import ru.yoo.sdk.kassa.payments.createGooglePayPaymentOptionWithFee
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.equalToDrawable
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeBankCard
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeGooglePay
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeLinkedCard
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeSbolSms
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeWallet
import ru.yoo.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoo.sdk.kassa.payments.model.Wallet
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
        assertThat(icon,
            equalToDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ym_ic_yoomoney
                )
            )
        )
    }

    @Test
    fun `get icon for AbstractWallet`() {
        // prepare
        val paymentOption = createAbstractWalletPaymentOption(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon,
            equalToDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ym_ic_yoomoney
                )
            )
        )
    }

    @Test
    fun `get icon for LinkedCard`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1) as LinkedCard

        // invoke
        val icon = paymentOption.getIcon(context) as InsetDrawable

        // assert
        assertThat(icon.drawable,
            equalToDrawable(
                AppCompatResources.getDrawable(
                    context,
                    paymentOption.brand.getIconResId()
                )
            )
        )
    }

    @Test
    fun `get icon for BankCard`() {
        // prepare
        val paymentOption = createNewCardPaymentOption(1)

        // invoke
        val icon = paymentOption.getIcon(context) as InsetDrawable

        // assert
        assertThat(icon.drawable,
            equalToDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ym_ic_add_card
                )
            )
        )
    }

    @Test
    fun `get icon for GooglePay`() {
        // prepare
        val paymentOption = createGooglePayPaymentOptionWithFee(1)

        // invoke
        val icon = paymentOption.getIcon(context)

        // assert
        assertThat(icon,
            equalToDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ym_ic_google_pay
                )
            )
        )
    }

    @Test
    fun `get title for Wallet`() {
        // prepare
        val paymentOption = createWalletPaymentOption(1) as Wallet

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title, equalTo(context.getText(R.string.ym_payment_option_yoomoney)))
    }

    @Test
    fun `get title for AbstractWallet`() {
        // prepare
        val paymentOption = createAbstractWalletPaymentOption(1)

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title, equalTo(context.getText(R.string.ym_payment_option_yoomoney)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard without name`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1) as LinkedCard

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title.toString(), equalTo("•••• " + paymentOption.pan.takeLast(4)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard with empty name`() {
        // prepare
        val paymentOption = (createLinkedCardPaymentOption(1) as LinkedCard).copy(name = "")

        // invoke
        val title = paymentOption.getTitle(context)

        // assert
        assertThat(title.toString(), equalTo("•••• " + paymentOption.pan.takeLast(4)))
        assertThat(title, instanceOf(String::class.java))
    }

    @Test
    fun `get title for LinkedCard with name`() {
        // prepare
        val paymentOption = (createLinkedCardPaymentOption(1) as LinkedCard).copy(name = "•••• 4321")

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
        initAppendableExtensions(context)
        val paymentOption = createWalletPaymentOption(1) as Wallet

        // invoke
        val additionalInfo = paymentOption.getAdditionalInfo() as SpannableStringBuilder

        // assert
        assertThat(additionalInfo.toString(), equalTo(paymentOption.balance.format().toString()))
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
        val actualConfirmations = paymentOptions.map { it.getConfirmation(redirectUrl) }

        // assert
        assertThat(actualConfirmations, contains(*expectedConfirmations))
    }
}
