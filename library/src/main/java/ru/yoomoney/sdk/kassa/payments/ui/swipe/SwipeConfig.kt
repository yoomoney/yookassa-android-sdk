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
import android.content.res.Resources
import android.util.SparseArray
import ru.yoomoney.sdk.kassa.payments.R

class SwipeConfig {
    val animationDuration: Int
    val menuOpenThreshold: Int
    val menuOpenedWidth: Int

    private constructor(res: Resources) {
        animationDuration = res.getInteger(android.R.integer.config_shortAnimTime)
        menuOpenThreshold = res.getDimensionPixelSize(R.dimen.ym_swipe_menu_button_width)
        menuOpenedWidth = menuOpenThreshold * 2
    }

    private constructor(
        animationDuration: Int,
        menuOpenThreshold: Int,
        menuItemsCount: Int
    ) {
        this.animationDuration = animationDuration
        this.menuOpenThreshold = menuOpenThreshold
        menuOpenedWidth = menuOpenThreshold * menuItemsCount
    }

    companion object {
        private val configurations = SparseArray<SwipeConfig>(2)

        fun get(context: Context): SwipeConfig {
            val metrics = context.resources.displayMetrics
            val density = (100.0f * metrics.density).toInt()
            var configuration = configurations[density]
            if (configuration == null) {
                configuration = SwipeConfig(context.resources)
                configurations.put(density, configuration)
            }
            return configuration
        }

        fun get(animationDuration: Int, menuOpenThreshold: Int, menuItemsCount: Int) =
            SwipeConfig(animationDuration, menuOpenThreshold, menuItemsCount)
    }
}