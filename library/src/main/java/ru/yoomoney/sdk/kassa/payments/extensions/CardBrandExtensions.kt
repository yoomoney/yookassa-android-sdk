/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.extensions

import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.AMERICAN_EXPRESS
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.BANK_CARD
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.CUP
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.DANKORT
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.DINERS_CLUB
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.DISCOVER_CARD
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.INSTA_PAYMENT
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.INSTA_PAYMENT_TM
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.JCB
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.LASER
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.MASTER_CARD
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.MIR
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.SOLO
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.SWITCH
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.UNKNOWN
import ru.yoomoney.sdk.kassa.payments.model.CardBrand.VISA

internal fun CardBrand.getIconResId(): Int = when (this) {
    MASTER_CARD -> R.drawable.ym_ic_card_type_mc_l
    MIR -> R.drawable.ym_ic_cardbrand_mir
    VISA -> R.drawable.ym_ic_card_type_visa_l
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
    UNKNOWN -> R.drawable.ym_ic_unknown_list
}
