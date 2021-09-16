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

package ru.yoomoney.sdk.kassa.payments.ui.swipe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

internal abstract class SwipeAnimation<T : SwipeAnimation.AnimationConfig?>(
    val swipeItem: SwipeItem, val config: T
) : AnimatorListenerAdapter() {

    private var animator: Animator? = null

    abstract fun prepareAnimator(): Animator

    fun start() {
        cancel()
        val prepareAnimator = prepareAnimator()
        prepareAnimator.duration = config!!.animationDuration.toLong()
        prepareAnimator.addListener(this)
        prepareAnimator.start()
        animator = prepareAnimator
    }

    fun cancel() {
        val cancelAnimator = animator
        if (cancelAnimator != null && cancelAnimator.isRunning) {
            cancelAnimator.cancel()
        }
    }

    override fun onAnimationStart(animation: Animator) {
        swipeItem.viewHolder().setIsRecyclable(false)
    }

    override fun onAnimationEnd(animation: Animator) {
        onPostAnimation()
    }

    override fun onAnimationCancel(animation: Animator) {
        onPostAnimation()
    }

    protected open fun onPostAnimation() {
        swipeItem.viewHolder().setIsRecyclable(true)
    }

    internal interface AnimationConfig {
        val animationDuration: Int
    }
}