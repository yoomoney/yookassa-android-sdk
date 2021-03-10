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

import android.animation.ValueAnimator
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams

fun ViewGroup.animateHeightChange(changeView: () -> Unit): ValueAnimator {
    val heightBefore = getViewHeight()
    changeView()
    val heightAfter = getViewHeight()
    return ValueAnimator.ofInt(heightBefore, heightAfter).apply {
        addUpdateListener { animation ->
            updateLayoutParams<ViewGroup.LayoutParams> {
                height = animation?.animatedValue as Int
            }
        }
        doOnEnd {
            updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT }
        }
        doOnCancel {
            updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT }
        }
        duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }
}
