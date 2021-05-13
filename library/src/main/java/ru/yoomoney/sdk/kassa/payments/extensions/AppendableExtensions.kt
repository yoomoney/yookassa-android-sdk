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

@file:JvmName("AppendableExtensions")

package ru.yoomoney.sdk.kassa.payments.extensions

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import kotlin.properties.Delegates

private const val ROUBLE_SCREW_X = -0.25f

private var roubleSign: CharSequence by Delegates.notNull()

internal fun initAppendableExtensions(context: Context) {
    roubleSign = SpannableStringBuilder().apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            append('Я')
            setSpan(RoubleTypefaceSpan(context.applicationContext), 0, 1, 0)
        } else {
            append('\u20BD')
        }
    }
}

internal fun <T : Appendable> T.appendRoubleSpan() =
        apply { append(roubleSign) }

private class RoubleTypefaceSpan(context: Context) : MetricAffectingSpan() {

    private val typeface = Typeface.createFromAsset(context.assets, "fonts/rouble.otf")

    override fun updateMeasureState(p: TextPaint) {
        update(p)
    }

    override fun updateDrawState(tp: TextPaint) {
        update(tp)
    }

    private fun update(paint: TextPaint) {
        val paintTypeface = paint.typeface
        val style = paintTypeface?.style ?: 0

        if (style and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (style and Typeface.ITALIC != 0) {
            paint.textSkewX = ROUBLE_SCREW_X
        }

        paint.typeface = typeface
    }
}
