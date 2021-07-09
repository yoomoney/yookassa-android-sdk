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

package ru.yoomoney.sdk.kassa.payments.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.View.ALPHA
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

internal class BottomSheetFragmentTransition : Transition() {

    override fun getTransitionProperties(): Array<String> = arrayOf(HEIGHT, STATE)

    override fun captureStartValues(transitionValues: TransitionValues) {
        val parentView = transitionValues.view.parent as? View ?: return

        transitionValues.values[HEIGHT] = transitionValues.view.height
        transitionValues.values[STATE] = START

        parentView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = parentView.height
        }
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        val parentView = transitionValues.view.parent as? View ?: return
        transitionValues.values[HEIGHT] = parentView.getViewHeight()
        transitionValues.values[STATE] = END
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }
        val parentView = endValues.view.parent as? View ?: return null
        val animators = listOf(
            prepareHeightAnimator(
                startValues.values[HEIGHT] as Int,
                endValues.values[HEIGHT] as Int,
                parentView
            ),
            prepareFadeInAnimator(endValues.view)
        )

        return AnimatorSet().apply {
            interpolator = FastOutSlowInInterpolator()
            duration = sceneRoot.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            playTogether(animators)
        }
    }

    private fun prepareFadeInAnimator(view: View): Animator = ObjectAnimator.ofFloat(view, ALPHA, 0f, 1f)

    private fun prepareHeightAnimator(startHeight: Int, endHeight: Int, container: View): ValueAnimator {
        return ValueAnimator.ofInt(startHeight, endHeight).apply {
            addUpdateListener { animation ->
                container.updateLayoutParams<ViewGroup.LayoutParams> { height = animation.animatedValue as Int }
            }
            doOnEnd {
                container.updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT }
            }
        }
    }

    companion object {
        private const val HEIGHT = "BottomSheetFragmentTransition:height"
        private const val STATE = "BottomSheetFragmentTransition:state"
        private const val START = "BottomSheetFragmentTransition:start"
        private const val END = "BottomSheetFragmentTransition:end"
    }
}
