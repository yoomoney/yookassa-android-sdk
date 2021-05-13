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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListIsEmptyException
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListErrorFormatter
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PaymentOptionListErrorFormatterTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val defaultErrorFormatter: ErrorFormatter = object: ErrorFormatter {
        override fun format(e: Throwable): CharSequence = e.toString()
    }
    private val context = RuntimeEnvironment.application

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testNoInternetException() {
        // prepare
        val testText = context.getText(R.string.ym_no_payment_options_error)
        val errorFormatter = PaymentOptionListErrorFormatter(context, defaultErrorFormatter)

        // invoke
        val formatText = errorFormatter.format(PaymentOptionListIsEmptyException())

        // assert
        assertThat(formatText, equalTo(testText))
    }

    @Test
    fun testOtherExceptions() {
        // prepare
        val testText = context.getText(R.string.ym_no_payment_options_error)
        val errorFormatter = PaymentOptionListErrorFormatter(context, defaultErrorFormatter)

        // invoke
        val formatTest = errorFormatter.format(Exception())

        // assert
        assertThat(formatTest, not(equalTo(testText)))
    }

    @Test
    fun should_ShowLogo_If_ShowLogoSet() {
        // prepare
        val errorForrmatter = PaymentOptionListErrorFormatter(context, defaultErrorFormatter)

        // invoke
        val formatText = errorForrmatter.format(Exception())

        // assert
        //assertThat("show logo", formatText.showLogo)
    }

    @Test
    fun should_HideLogo_If_ShowLogoNotSet() {
        // prepare
        val errorFormatter = PaymentOptionListErrorFormatter(context, defaultErrorFormatter)

        // invoke
        val viewModel = errorFormatter.format(Exception())

        // assert
        //assertThat("hide logo", !viewModel.showLogo)
    }
}