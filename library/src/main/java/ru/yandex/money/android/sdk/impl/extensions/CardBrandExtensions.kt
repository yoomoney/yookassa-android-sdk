/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

@file:JvmName("CardBrandExtensions")

package ru.yandex.money.android.sdk.impl.extensions

import ru.yandex.money.android.sdk.CardBrand
import ru.yandex.money.android.sdk.CardBrand.AMERICAN_EXPRESS
import ru.yandex.money.android.sdk.CardBrand.BANK_CARD
import ru.yandex.money.android.sdk.CardBrand.CUP
import ru.yandex.money.android.sdk.CardBrand.DANKORT
import ru.yandex.money.android.sdk.CardBrand.DINERS_CLUB
import ru.yandex.money.android.sdk.CardBrand.DISCOVER_CARD
import ru.yandex.money.android.sdk.CardBrand.INSTA_PAYMENT
import ru.yandex.money.android.sdk.CardBrand.INSTA_PAYMENT_TM
import ru.yandex.money.android.sdk.CardBrand.JCB
import ru.yandex.money.android.sdk.CardBrand.LASER
import ru.yandex.money.android.sdk.CardBrand.MASTER_CARD
import ru.yandex.money.android.sdk.CardBrand.MIR
import ru.yandex.money.android.sdk.CardBrand.SOLO
import ru.yandex.money.android.sdk.CardBrand.SWITCH
import ru.yandex.money.android.sdk.CardBrand.UNKNOWN
import ru.yandex.money.android.sdk.CardBrand.VISA
import ru.yandex.money.android.sdk.R

internal fun CardBrand.getIconResId(): Int = when (this) {
    MASTER_CARD -> R.drawable.ym_ic_cardbrand_mastercard
    MIR -> R.drawable.ym_ic_cardbrand_mir
    VISA -> R.drawable.ym_ic_cardbrand_visa
    AMERICAN_EXPRESS -> R.drawable.ym_ic_cardbrand_american_express
    JCB -> R.drawable.ym_ic_cardbrand_jcb
    CUP -> R.drawable.ym_ic_cardbrand_cup
    DINERS_CLUB -> R.drawable.ym_ic_cardbrand_diners_club
    BANK_CARD -> R.drawable.ym_ic_cardbrand_bank_card
    DISCOVER_CARD -> R.drawable.ym_ic_cardbrand_discover_card
    INSTA_PAYMENT -> R.drawable.ym_ic_cardbrand_instapay
    INSTA_PAYMENT_TM -> R.drawable.ym_ic_cardbrand_instapay
    LASER -> R.drawable.ym_ic_cardbrand_laser
    DANKORT -> R.drawable.ym_ic_cardbrand_dankort
    SOLO -> R.drawable.ym_ic_cardbrand_solo
    SWITCH -> R.drawable.ym_ic_cardbrand_switch
    UNKNOWN -> R.drawable.ym_ic_cardbrand_unknown
}
