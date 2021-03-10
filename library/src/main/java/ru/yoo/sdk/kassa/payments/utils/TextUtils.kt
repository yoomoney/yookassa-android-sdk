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

package ru.yoo.sdk.kassa.payments.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import ru.yoo.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

fun getMessageWithLink(
    context: Context,
    firstMessagePartRes: Int,
    secondMessagePartRes: Int,
    action: () -> Unit
): CharSequence {
    val firstMessagePart = context.getText(firstMessagePartRes)
    val secondMessagePart = context.getText(secondMessagePartRes)
    return SpannableStringBuilder(
        "$firstMessagePart $secondMessagePart"
    ).apply {
        setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    action()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.apply {
                        color = InMemoryColorSchemeRepository.colorScheme.primaryColor
                        isUnderlineText = false
                    }
                }
            },
            length - secondMessagePart.length,
            length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
