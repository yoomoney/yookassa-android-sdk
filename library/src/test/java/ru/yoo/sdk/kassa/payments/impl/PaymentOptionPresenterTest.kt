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

package ru.yoo.sdk.kassa.payments.impl

import android.text.Spannable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import ru.yoo.sdk.kassa.payments.createAbstractWalletPaymentOption
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.impl.extensions.format
import ru.yoo.sdk.kassa.payments.impl.extensions.initExtensions
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentOptionPresenter
import ru.yoo.sdk.kassa.payments.model.PaymentOption

private const val TEST_ID = 1

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [27])
internal class PaymentOptionPresenterTest(
    private val testOption: PaymentOption
) {

    companion object {
        @[ParameterizedRobolectricTestRunner.Parameters(name = "{0}") JvmStatic]
        fun data() = listOf(
            arrayOf(createNewCardPaymentOption(TEST_ID)),
            arrayOf(createWalletPaymentOption(TEST_ID)),
            arrayOf(createAbstractWalletPaymentOption(TEST_ID)),
            arrayOf(createLinkedCardPaymentOption(TEST_ID)),
            arrayOf(createSbolSmsInvoicingPaymentOption(TEST_ID))
        )
    }

    @Test
    fun testPresentPaymentOption() {
        // prepare
        val context = RuntimeEnvironment.application
        initExtensions(context)
        val paymentOptionPresenter = PaymentOptionPresenter(context)

        // invoke
        val viewModel = paymentOptionPresenter(testOption)

        // assert
        assertThat(viewModel.optionId, equalTo(testOption.id))


        val testCharge = clearSpans(testOption.charge.format() as Spannable)
        val amount = clearSpans(viewModel.amount as Spannable)
        assertThat(amount, equalTo(testCharge))
    }

    fun clearSpans(spannable: Spannable) {
        spannable.getSpans(0, spannable.length, Any::class.java).forEach(spannable::removeSpan)
    }
}