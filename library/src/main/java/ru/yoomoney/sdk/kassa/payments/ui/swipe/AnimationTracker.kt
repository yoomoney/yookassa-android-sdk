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

import androidx.annotation.UiThread
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.RecyclerView
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeItem.RecoverAnimationType
import java.util.ArrayList
import java.util.HashSet

@UiThread
internal class AnimationTracker private constructor(vararg trackingAnimations: RecoverAnimationType) {

    private val runningAnimations: ArrayMap<RecoverAnimationType, ArrayList<RecyclerView.ViewHolder>>
    private val finishedAnimations: ArrayMap<RecoverAnimationType, ArrayList<RecyclerView.ViewHolder>>
    private val animatedViewHolders = HashSet<RecyclerView.ViewHolder>(DEFAULT_CAPACITY)

    fun start(viewHolder: RecyclerView.ViewHolder, type: RecoverAnimationType) {
        checkWhetherAnimationTracking(type)
        runningAnimations[type]?.add(viewHolder)
        animatedViewHolders.add(viewHolder)
    }

    fun finish(viewHolder: RecyclerView.ViewHolder, type: RecoverAnimationType) {
        checkWhetherAnimationTracking(type)
        runningAnimations[type]?.remove(viewHolder)
        finishedAnimations[type]?.add(viewHolder)
    }

    fun reset(type: RecoverAnimationType) {
        checkWhetherAnimationTracking(type)
        runningAnimations[type]?.let { reset(it) }
        finishedAnimations[type]?.let { reset(it) }
    }

    fun isTrackingType(type: RecoverAnimationType): Boolean {
        return runningAnimations[type] != null && finishedAnimations[type] != null
    }

    fun hasRunningAnimations(type: RecoverAnimationType): Boolean {
        checkWhetherAnimationTracking(type)
        return runningAnimations[type]?.isNotEmpty() == true
    }

    fun isAnimated(viewHolder: RecyclerView.ViewHolder): Boolean {
        return animatedViewHolders.contains(viewHolder)
    }

    private fun reset(viewHolders: ArrayList<RecyclerView.ViewHolder>) {
        animatedViewHolders.removeAll(viewHolders)
        viewHolders.clear()
    }

    private fun checkWhetherAnimationTracking(type: RecoverAnimationType) {
        require(isTrackingType(type)) {
            "Animation tracker monitors only " + runningAnimations.keys + " but you try " + type
        }
    }

    companion object {
        private const val DEFAULT_CAPACITY = 2

        @JvmStatic
        fun withPossibleValues(vararg dismiss: RecoverAnimationType): AnimationTracker {
            return AnimationTracker(*dismiss)
        }
    }

    init {
        require(trackingAnimations.isNotEmpty()) { "You must set at least one tracking animation" }
        runningAnimations = ArrayMap(trackingAnimations.size)
        finishedAnimations = ArrayMap(trackingAnimations.size)
        for (type in trackingAnimations) {
            runningAnimations[type] = ArrayList(DEFAULT_CAPACITY)
            finishedAnimations[type] = ArrayList(DEFAULT_CAPACITY)
        }
    }
}