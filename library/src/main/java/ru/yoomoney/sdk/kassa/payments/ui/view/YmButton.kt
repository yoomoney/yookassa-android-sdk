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

package ru.yoomoney.sdk.kassa.payments.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository.colorScheme
import ru.yoomoney.sdk.kassa.payments.extensions.highlight
import ru.yoomoney.sdk.kassa.payments.extensions.isLightColor

internal class YmButton
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        val primaryColor = colorScheme.primaryColor
        setBackgroundDrawable(StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), GradientDrawable().apply {
                setColor(primaryColor.highlight())
                cornerRadius = context.resources.getDimension(R.dimen.ym_space_xs)
            })
            addState(intArrayOf(android.R.attr.state_enabled), GradientDrawable().apply {
                setColor(primaryColor)
                cornerRadius = context.resources.getDimension(R.dimen.ym_space_xs)
            })
            addState(
                StateSet.WILD_CARD,
                GradientDrawable().apply {
                    setColor(ContextCompat.getColor(context, R.color.ym_button_primary_disabled))
                    cornerRadius = context.resources.getDimension(R.dimen.ym_space_xs)
                }
            )
        })

        setTextColor(
            ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.ym_hint),
                    if (primaryColor.isLightColor()) Color.BLACK else Color.WHITE
                )
            )
        )
    }
}
