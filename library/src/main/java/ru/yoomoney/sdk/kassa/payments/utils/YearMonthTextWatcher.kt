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

package ru.yoomoney.sdk.kassa.payments.utils

import android.text.Editable

internal class YearMonthTextWatcher : SimpleTextWatcher {

    private var previous: String? = null

    override fun afterTextChanged(s: Editable) {
        var value = s.toString()

        if (value == previous) {
            return
        }

        previous = value
        value = value.filter(Char::isDigit)

        val length = value.length

        if (length > 0) {
            when (value[0]) {
                '0' -> {
                    if (length > 1 && value[1] == '0') {
                        value = value.substring(0, 1)
                    }
                    value = format(value)
                }
                '1' -> if (length > 1) {
                    value = when (value[1]) {
                        '0', '1', '2' -> format(value)
                        else -> value.substring(0, 1)
                    }
                }
                else -> value = if (length == 1) "0" + value else ""
            }
        }
        s.replace(0, s.length, value)
    }

    private fun format(value: String): String {
        val length = value.length
        return if (length > 2) {
            value.substring(0, 2) + '/' + value.substring(2, length)
        } else {
            value
        }
    }
}
