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

package ru.yoo.sdk.kassa.payments.impl.payment

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoo.sdk.kassa.payments.impl.ApiMethodException
import ru.yoo.sdk.kassa.payments.model.ErrorCode

@RunWith(Parameterized::class)
class PaymentErrorPresenterTest(
        private val exception: Exception,
        private val shouldPass: Boolean
) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<Any>> {
            val ret = mutableListOf<Array<Any>>()

            ErrorCode.values().forEach {
                ret.add(arrayOf(ApiMethodException(it), it == ErrorCode.INTERNAL_SERVER_ERROR || it == ErrorCode.UNKNOWN))
            }

            ret.add(arrayOf(Exception(), true))

            return ret
        }
    }

    private val presenter = PaymentErrorPresenter(Exception::toString)

    @Test
    fun name() {
        // prepare

        // invoke
        val result = try {
            presenter(exception)
        } catch (e: IllegalStateException) {
            e
        }

        // assert
        if (shouldPass) {
            assertThat(result, instanceOf(CharSequence::class.java))
        }
    }
}