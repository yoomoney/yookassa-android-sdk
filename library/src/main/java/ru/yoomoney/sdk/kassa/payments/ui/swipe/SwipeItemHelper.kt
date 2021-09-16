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

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import ru.yoomoney.sdk.kassa.payments.ui.swipe.AnimationTracker.Companion.withPossibleValues
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeItem.Presenter
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeItem.RecoverAnimationType
import kotlin.math.abs

internal class SwipeItemHelper private constructor(
    context: Context,
    swipeConfig: SwipeConfig,
    private val dragListener: DragListener?
) : OnItemTouchListener, OnChildAttachStateChangeListener, SwipeItem.AnimationCallback {

    val animationTracker = withPossibleValues(RecoverAnimationType.OPEN_MENU)
    var recyclerView: RecyclerView? = null

    private val config: SwipeConfig
    private val touchSlop: Int

    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var currentSwipeItem: SwipeItem? = null

    constructor(context: Context, swipeConfig: SwipeConfig) : this(context, swipeConfig, null)

    @Synchronized
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnItemTouchListener(this)
        recyclerView.addOnChildAttachStateChangeListener(this)
    }

    @Synchronized
    fun detachFromRecyclerView() {
        val recyclerView = this.recyclerView
        recyclerView?.let {
            recyclerView.removeOnItemTouchListener(this)
            recyclerView.removeOnChildAttachStateChangeListener(this)
            this.recyclerView = null
        }
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
        return when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = motionEvent.getPointerId(0)
                initialTouchX = motionEvent.x
                initialTouchY = motionEvent.y
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    return false
                }
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    currentSwipeItem?.let {
                        currentSwipeItem?.cancel()
                        currentSwipeItem = null
                    }
                    return false
                }
                val dX = motionEvent.x - initialTouchX
                val dY = motionEvent.y - initialTouchY
                val absDx = abs(dX)
                val absDy = abs(dY)
                if (absDx > touchSlop && absDx > absDy) {
                    val selectedViewHolder = getViewHolderFromMotionEvent(recyclerView, motionEvent)
                    if (selectedViewHolder is Presenter) {
                        if (animationTracker.isAnimated(selectedViewHolder)) {
                            return false
                        }
                        if (currentSwipeItem?.viewHolder() !== selectedViewHolder) {
                            currentSwipeItem?.cancel()
                        }
                        currentSwipeItem = SwipeItem(selectedViewHolder, config, this)
                        if (currentSwipeItem?.isSwipeAvailable() == false) {
                            return false
                        }
                        if (currentSwipeItem?.isDismissed == false) {
                            dragListener?.onStartDrag()
                        } else {
                            currentSwipeItem = null
                        }
                    }
                    currentSwipeItem != null
                } else {
                    false
                }
            }
            else -> false
        }
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {
        if (currentSwipeItem == null) {
            return
        }
        val touchDx = motionEvent.x - initialTouchX
        val swipeDx = currentSwipeItem?.getSwipeTranslation(touchDx)
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_MOVE -> swipeDx?.let { currentSwipeItem?.translateItem(it) }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                swipeDx?.let { currentSwipeItem?.animateFrom(it) }
                dragListener?.onStopDrag()
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit

    override fun onChildViewAttachedToWindow(view: View) = Unit

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    override fun onAnimationStart(
        item: SwipeItem,
        type: RecoverAnimationType,
        withSwipe: Boolean
    ) {
        if (animationTracker.isTrackingType(type)) {
            animationTracker.start(item.viewHolder(), type)
        }
        if (currentSwipeItem == item && !shouldKeepSwipeItem(type)) {
            currentSwipeItem = null
        }
    }

    override fun onAnimationEnd(
        item: SwipeItem,
        type: RecoverAnimationType,
        withSwipe: Boolean
    ) {
        if (animationTracker.isTrackingType(type)) {
            animationTracker.finish(item.viewHolder(), type)
            if (!animationTracker.hasRunningAnimations(type)) {
                postCallback(type)
            }
        }
    }

    private fun shouldKeepSwipeItem(type: RecoverAnimationType): Boolean {
        return type === RecoverAnimationType.OPEN_MENU
    }

    private fun getViewHolderFromMotionEvent(
        recyclerView: RecyclerView,
        motionEvent: MotionEvent
    ): RecyclerView.ViewHolder? {
        val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
        return child?.run { recyclerView.getChildViewHolder(this) }
    }

    fun forceCancel() {
        currentSwipeItem?.cancel()
    }

    private fun postCallback(animationType: RecoverAnimationType) {
        recyclerView?.post(object : Runnable {
            override fun run() {
                if (recyclerView != null && recyclerView?.isAttachedToWindow == true) {
                    val animator = recyclerView?.itemAnimator
                    if (animator == null || !animator.isRunning) {
                        animationTracker.reset(animationType)
                    } else {
                        recyclerView?.post(this)
                    }
                }
            }
        })
    }

    internal interface DragListener {
        fun onStartDrag()
        fun onStopDrag()
    }

    init {
        val viewConfiguration = ViewConfiguration.get(context)
        touchSlop = viewConfiguration.scaledTouchSlop
        config = swipeConfig
    }
}
