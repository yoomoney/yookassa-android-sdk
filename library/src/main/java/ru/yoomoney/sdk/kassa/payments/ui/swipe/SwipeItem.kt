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

import android.util.Property
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class SwipeItem internal constructor(
    private val viewHolder: RecyclerView.ViewHolder,
    config: SwipeConfig,
    animationCallback: AnimationCallback
) {
    val contentContainer: View
    val config: SwipeConfig
    val animationCallback: AnimationCallback

    private val swipeMenuContainer: View
    private val width: Int
    private val maxRightOffset: Int
    private val startDx: Float
    private val isSwipeItemAvailable: Boolean

    val isDismissed: Boolean
        get() = contentContainer.translationX.roundToInt() == -width

    fun viewHolder(): RecyclerView.ViewHolder {
        return viewHolder
    }

    fun cancel() {
        animate(RecoverAnimationType.CANCEL, false)
    }

    fun getSwipeTranslation(touchDx: Float): Float {
        val translation = startDx + touchDx
        return if (translation < 0) max(translation, -maxRightOffset.toFloat()) else 0F
    }

    fun isSwipeAvailable(): Boolean = isSwipeItemAvailable

    fun translateItem(translationX: Float) {
        setContentTranslation(translationX)
        showOnlyInvolvedHints(translationX)
    }

    fun animateFrom(translationX: Float) {
        val animationType: RecoverAnimationType
        val absDx = abs(translationX)
        animationType = if (absDx < config.menuOpenThreshold) {
            RecoverAnimationType.CANCEL
        } else {
            RecoverAnimationType.OPEN_MENU
        }
        animate(animationType, true)
    }

    fun setContentTranslation(translationX: Float) {
        contentContainer.translationX = translationX
        if (translationX < 0f) {
            swipeMenuContainer.translationX = translationX + width
        }
    }

    fun showOnlyInvolvedHints(translationX: Float) {
        when {
            translationX > 0f -> {
                hideView(swipeMenuContainer)
            }
            translationX < 0f -> {
                showView(swipeMenuContainer)
            }
            else -> {
                hideView(swipeMenuContainer)
            }
        }
    }

    private fun animate(type: RecoverAnimationType, withSwipe: Boolean) {
        val recoverAnimation = RecoverAnimation(this, RecoverAnimationConfig(type, withSwipe))
        recoverAnimation.start()
    }

    private inner class RecoverAnimationConfig(
        val type: RecoverAnimationType,
        val withSwipe: Boolean
    ) : RecoverAnimation.AnimationConfig {
        override val animationDuration: Int
            get() = config.animationDuration

        override val targetTranslation: Float
            get() = when (type) {
                RecoverAnimationType.CANCEL -> 0f
                RecoverAnimationType.OPEN_MENU -> (-config.menuOpenedWidth).toFloat()
            }

        override fun getAnimationListener(): RecoverAnimation.AnimationListener {
            return object : RecoverAnimation.AnimationListener {
                override fun onPreAnimation() {
                    animationCallback.onAnimationStart(this@SwipeItem, type, withSwipe)
                }

                override fun onPostAnimation() {
                    showOnlyInvolvedHints(contentContainer.translationX)
                    animationCallback.onAnimationEnd(this@SwipeItem, type, withSwipe)
                }
            }
        }
    }

    interface Presenter {
        val isSwipeAvailable: Boolean
        fun getContentContainer(): View
        fun getSwipeMenuContainer(): View
    }

    interface AnimationCallback {
        fun onAnimationStart(item: SwipeItem, type: RecoverAnimationType, withSwipe: Boolean)
        fun onAnimationEnd(item: SwipeItem, type: RecoverAnimationType, withSwipe: Boolean)
    }

    enum class RecoverAnimationType {
        CANCEL, OPEN_MENU
    }

    companion object {
        val CONTENT_TRANSLATION_X: Property<SwipeItem, Float> = object : Property<SwipeItem, Float>(
            Float::class.java, "contentTranslationX"
        ) {
            override fun get(swipeItem: SwipeItem): Float {
                return swipeItem.contentContainer.translationX
            }

            override fun set(swipeItem: SwipeItem, value: Float) {
                swipeItem.setContentTranslation(value)
            }
        }

        private fun showView(view: View) {
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }

        private fun hideView(view: View) {
            if (view.visibility != View.INVISIBLE) {
                view.visibility = View.INVISIBLE
            }
        }
    }

    init {
        contentContainer = (viewHolder as Presenter).getContentContainer()
        swipeMenuContainer = (viewHolder as Presenter).getSwipeMenuContainer()
        this.isSwipeItemAvailable = (viewHolder as Presenter).isSwipeAvailable
        this.config = config
        this.animationCallback = animationCallback
        width = viewHolder.itemView.width
        maxRightOffset = swipeMenuContainer.width
        startDx = contentContainer.translationX
    }
}