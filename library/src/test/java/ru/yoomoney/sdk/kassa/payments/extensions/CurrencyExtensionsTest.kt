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

package ru.yoomoney.sdk.kassa.payments.extensions

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.extensions.isRub
import ru.yoomoney.sdk.kassa.payments.extensions.toNumberFormat
import java.text.NumberFormat
import java.util.Currency

class CurrencyExtensionsTest {

    @Test
    fun `should return true for RUB Currency instance`() {
        // prepare
        val currency = Currency.getInstance("RUB")

        // invoke
        val isRub = currency.isRub

        // assert
        assertThat("should be true", isRub)
    }

    @Test
    fun `should be false for nonRUB Currency`() {
        // prepare
        val currency = Currency.getInstance("USD")

        // invoke
        val isRub = currency.isRub

        // assert
        assertThat("should be false", !isRub)
    }

    @Test
    fun `should return custom format for RUB Currency`() {
        // prepare
        val currency = Currency.getInstance("RUB")
        val defaultFormat = NumberFormat.getCurrencyInstance().apply { this.currency = currency }

        // invoke
        val actualFormat = currency.toNumberFormat()

        // assert
        assertThat(actualFormat, not(equalTo(defaultFormat)))
    }

    @Test
    fun `should return default format for nonRUB Currency`() {
        // prepare
        val currency = Currency.getInstance("USD")
        val defaultFormat = NumberFormat.getCurrencyInstance().apply { this.currency = currency }

        // invoke
        val actualFormat = currency.toNumberFormat()

        // assert
        assertThat(actualFormat, equalTo(defaultFormat))
    }
}