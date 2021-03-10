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

package ru.yoo.sdk.kassa.payments.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import ru.yoo.sdk.gui.widget.TextInputView
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoo.sdk.kassa.payments.utils.setUpCursorColor


class CheckoutTextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
):  TextInputView(context, attrs, defStyleAttr) {

    init {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tintColorStateList(typeColorStateList(context))
                editText.setUpCursorColor(InMemoryColorSchemeRepository.colorScheme.primaryColor)
            }
        }
    }

    private fun typeColorStateList(context: Context): ColorStateList {
        val color = intArrayOf(
            ContextCompat.getColor(context, R.color.color_type_disable),
            InMemoryColorSchemeRepository.colorScheme.primaryColor
        )
        return ColorStateList(stateArray(), color)
    }

    private fun stateArray() = arrayOf(
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled)
    )
}