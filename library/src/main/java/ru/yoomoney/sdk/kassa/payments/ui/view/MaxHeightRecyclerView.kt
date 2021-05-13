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
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import ru.yoomoney.sdk.kassa.payments.R

internal class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var maxHeight: Int = 0

    init {
        with(context.theme.obtainStyledAttributes(attrs, R.styleable.ym_MaxHeightRecyclerView, defStyleAttr, 0)) {
            if (hasValue(R.styleable.ym_MaxHeightRecyclerView_ym_maxHeight)) {
                maxHeight = getDimensionPixelSize(R.styleable.ym_MaxHeightRecyclerView_ym_maxHeight, 0)
            }
            recycle()
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val newHeightSpec = maxHeight
            .takeUnless(0::equals)
            ?.takeIf { MeasureSpec.getSize(heightSpec) > it }
            ?.let {
                MeasureSpec.makeMeasureSpec(it, MeasureSpec.AT_MOST)
            } ?: heightSpec
        super.onMeasure(widthSpec, newHeightSpec)
    }
}
