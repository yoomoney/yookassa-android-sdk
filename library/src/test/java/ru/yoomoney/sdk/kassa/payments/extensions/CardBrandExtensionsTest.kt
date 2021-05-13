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

package ru.yoomoney.sdk.kassa.payments.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.extensions.getIconResId
import ru.yoomoney.sdk.kassa.payments.model.CardBrand

@RunWith(Parameterized::class)
internal class CardBrandExtensionsTest(val args: Pair<CardBrand, Int>) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Pair<CardBrand, Int>> {
            return listOf(
                    CardBrand.MASTER_CARD to R.drawable.ym_ic_card_type_mc_l,
                    CardBrand.MIR to R.drawable.ym_ic_cardbrand_mir,
                    CardBrand.VISA to R.drawable.ym_ic_card_type_visa_l,
                    CardBrand.AMERICAN_EXPRESS to R.drawable.ym_ic_cardbrand_american_express,
                    CardBrand.JCB to R.drawable.ym_ic_cardbrand_jcb,
                    CardBrand.CUP to R.drawable.ym_ic_cardbrand_cup,
                    CardBrand.DINERS_CLUB to R.drawable.ym_ic_cardbrand_diners_club,
                    CardBrand.BANK_CARD to R.drawable.ym_ic_cardbrand_bank_card,
                    CardBrand.DISCOVER_CARD to R.drawable.ym_ic_cardbrand_discover_card,
                    CardBrand.INSTA_PAYMENT to R.drawable.ym_ic_cardbrand_instapay,
                    CardBrand.INSTA_PAYMENT_TM to R.drawable.ym_ic_cardbrand_instapay,
                    CardBrand.LASER to R.drawable.ym_ic_cardbrand_laser,
                    CardBrand.DANKORT to R.drawable.ym_ic_cardbrand_dankort,
                    CardBrand.SOLO to R.drawable.ym_ic_cardbrand_solo,
                    CardBrand.SWITCH to R.drawable.ym_ic_cardbrand_switch,
                    CardBrand.UNKNOWN to R.drawable.ym_ic_unknown_list)
        }
    }

    @Test
    fun name() {
        val (brand, resourceId) = args

        assertThat(brand.getIconResId(), equalTo(resourceId))
    }
}