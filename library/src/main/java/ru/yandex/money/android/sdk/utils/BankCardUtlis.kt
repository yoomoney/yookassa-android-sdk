/*
 * The MIT License (MIT)
 * Copyright © 2019 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.utils

import android.support.annotation.DrawableRes
import ru.yandex.money.android.sdk.R

private const val VISA = "4"
private const val MASTERCARD = "5"
private var UNKNOWN_CARD_ICON = R.drawable.ym_ic_unknown_list

private val banks by lazy { initBanks() }

@DrawableRes
fun getBankLogo(pan: String): Int {
    return getIconByPan(pan)
}

@DrawableRes
private fun getIconByPan(pan: String): Int {
    val panInput = pan.replace(" ", "").take(6)
    var icon = UNKNOWN_CARD_ICON
    banks.forEach { bank ->
        bank.bins.find { it == panInput }?.let {
            icon = bank.icon
            return@forEach
        }
    }
    if (icon == UNKNOWN_CARD_ICON) {
        icon = when {
            pan.startsWith(VISA) -> R.drawable.ym_ic_card_type_visa_l
            pan.startsWith(MASTERCARD) -> R.drawable.ym_ic_card_type_mc_l
            else -> UNKNOWN_CARD_ICON
        }
    }
    return icon
}