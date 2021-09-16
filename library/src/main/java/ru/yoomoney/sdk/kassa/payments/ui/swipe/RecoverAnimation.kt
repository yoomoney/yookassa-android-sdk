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
import android.animation.ObjectAnimator

internal class RecoverAnimation(
    item: SwipeItem,
    config: AnimationConfig
) : SwipeAnimation<RecoverAnimation.AnimationConfig>(item, config) {

    private val animationListener: AnimationListener? = config.getAnimationListener()

    override fun prepareAnimator(): Animator {
        return ObjectAnimator.ofFloat(swipeItem, SwipeItem.CONTENT_TRANSLATION_X, config.targetTranslation)
    }

    override fun onAnimationStart(animation: Animator) {
        super.onAnimationStart(animation)
        animationListener?.onPreAnimation()
    }

    override fun onPostAnimation() {
        super.onPostAnimation()
        animationListener?.onPostAnimation()
    }

    internal interface AnimationListener {
        fun onPreAnimation()
        fun onPostAnimation()
    }

    internal interface AnimationConfig : SwipeAnimation.AnimationConfig {
        val targetTranslation: Float
        fun getAnimationListener(): AnimationListener?
    }
}