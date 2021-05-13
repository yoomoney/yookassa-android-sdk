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

package ru.yoomoney.sdk.kassa.payments.paymentOptionInfo

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.paymentOptionInfo.isCorrectPan

@RunWith(Parameterized::class)
class LuhnKtTest(private val args: Pair<String, Boolean>) {

    companion object {
        @[JvmStatic Parameterized.Parameters(name = "{0}")]
        fun data() = listOf(
            "qwerty" to false,
            "000000000000" to false,
            "4807864936541" to true,
            "39434564086011" to true,
            "216429335178779" to true,
            "4124796630572739" to true,
            "17630931382938361" to true,
            "707478195647148106" to true,
            "2458785214790519942" to true,
            "00000000000000000000" to false,
            "0000000000000000" to true,
            "0000000000000001" to false,
            "4444444444444443" to false,
            "4444444444444448" to true,
            "4644444444444453" to true,
            "8845152368947322" to true,
            "1234567887654321" to false,
            "4414424454748443" to false,
            "9876543212345678" to false,
            "1236546786547854" to false,
            "5645562840583626" to false
        )
    }

    @Test
    fun isCorrectPan() {
        val (pan, result) = args
        assertThat(pan.isCorrectPan(), equalTo(result))
    }

}
