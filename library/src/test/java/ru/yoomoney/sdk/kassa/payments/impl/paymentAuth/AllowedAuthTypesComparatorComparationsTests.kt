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

package ru.yoomoney.sdk.kassa.payments.impl.paymentAuth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthType.EMERGENCY
import ru.yoomoney.sdk.kassa.payments.model.AuthType.SECURE_PASSWORD
import ru.yoomoney.sdk.kassa.payments.model.AuthType.SMS
import ru.yoomoney.sdk.kassa.payments.model.AuthType.TOTP
import ru.yoomoney.sdk.kassa.payments.paymentAuth.AllowedAuthTypesComparator

@RunWith(Parameterized::class)
internal class AllowedAuthTypesComparatorComparationsTests(
    private val input: Array<AuthType>,
    private val expected: Array<AuthType>
) {
    companion object {
        @[Parameterized.Parameters() JvmStatic]
        fun data() = mutableListOf(
                arrayOf(arrayOf(EMERGENCY, SMS), arrayOf(SMS, EMERGENCY)),
                arrayOf(arrayOf(EMERGENCY, TOTP), arrayOf(TOTP, EMERGENCY)),
                arrayOf(arrayOf(EMERGENCY, SECURE_PASSWORD), arrayOf(SECURE_PASSWORD, EMERGENCY)),
                arrayOf(arrayOf(SECURE_PASSWORD, SMS), arrayOf(SMS, SECURE_PASSWORD)),
                arrayOf(arrayOf(SECURE_PASSWORD, TOTP), arrayOf(TOTP, SECURE_PASSWORD)),
                arrayOf(arrayOf(TOTP, SMS), arrayOf(SMS, TOTP)),
                arrayOf(arrayOf(SMS, TOTP), arrayOf(SMS, TOTP)),
                arrayOf(arrayOf(SMS, SECURE_PASSWORD), arrayOf(SMS, SECURE_PASSWORD)),
                arrayOf(arrayOf(SMS, EMERGENCY), arrayOf(SMS, EMERGENCY)),
                arrayOf(arrayOf(TOTP, SECURE_PASSWORD), arrayOf(TOTP, SECURE_PASSWORD)),
                arrayOf(arrayOf(TOTP, EMERGENCY), arrayOf(TOTP, EMERGENCY)),
                arrayOf(arrayOf(SECURE_PASSWORD, EMERGENCY), arrayOf(SECURE_PASSWORD, EMERGENCY)),
                arrayOf(arrayOf(null, SMS), arrayOf(SMS, null)),
                arrayOf(arrayOf(null, TOTP), arrayOf(TOTP, null)),
                arrayOf(arrayOf(null, SECURE_PASSWORD), arrayOf(SECURE_PASSWORD, null)),
                arrayOf(arrayOf(null, EMERGENCY), arrayOf(EMERGENCY, null)),
                arrayOf(arrayOf(SMS, null), arrayOf(SMS, null)),
                arrayOf(arrayOf(TOTP, null), arrayOf(TOTP, null)),
                arrayOf(arrayOf(SECURE_PASSWORD, null), arrayOf(SECURE_PASSWORD, null)),
                arrayOf(arrayOf(EMERGENCY, null), arrayOf(EMERGENCY, null))
        )
    }

    private val comparator = AllowedAuthTypesComparator()

    @Test
    fun testCompare() {
        // prepare

        // invoke
        val sorted = input.sortedWith(comparator)

        // assert
        val inputString = input.joinToString(prefix = "[", postfix = "]")
        val sortedString = sorted.joinToString(prefix = "[", postfix = "]")
        val expectedString = expected.joinToString(prefix = "[", postfix = "]")
        val message = "\ninput    $inputString" +
                      "\nsorted   $sortedString" +
                      "\nexpected $expectedString"
        (0 until sorted.size).forEach {
            assertThat(message, sorted[it], equalTo(expected[it]))
        }
    }
}