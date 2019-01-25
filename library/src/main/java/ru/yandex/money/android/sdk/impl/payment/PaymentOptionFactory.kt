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

package ru.yandex.money.android.sdk.impl.payment

import org.json.JSONObject
import ru.yandex.money.android.sdk.PaymentMethodType.BANK_CARD
import ru.yandex.money.android.sdk.PaymentMethodType.GOOGLE_PAY
import ru.yandex.money.android.sdk.PaymentMethodType.SBERBANK
import ru.yandex.money.android.sdk.PaymentMethodType.YANDEX_MONEY
import ru.yandex.money.android.sdk.impl.extensions.getCardBrand
import ru.yandex.money.android.sdk.impl.extensions.getPaymentMethodType
import ru.yandex.money.android.sdk.impl.extensions.toAmount
import ru.yandex.money.android.sdk.impl.payment.InstrumentType.LINKED_BANK_CARD
import ru.yandex.money.android.sdk.impl.payment.InstrumentType.UNKNOWN
import ru.yandex.money.android.sdk.impl.payment.InstrumentType.WALLET
import ru.yandex.money.android.sdk.model.AbstractWallet
import ru.yandex.money.android.sdk.model.GooglePay
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.NewCard
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.SbolSmsInvoicing
import ru.yandex.money.android.sdk.model.Wallet

internal fun paymentOptionFactory(
    id: Int,
    jsonObject: JSONObject,
    userName: String
): PaymentOption? {

    val charge = jsonObject.getJSONObject("charge").toAmount()
    val paymentMethodType = jsonObject.getPaymentMethodType()

    return paymentMethodType?.let {
        when (paymentMethodType) {
            BANK_CARD -> NewCard(id, charge, null)
            SBERBANK -> SbolSmsInvoicing(id, charge, null)
            GOOGLE_PAY -> GooglePay(id, charge, null)
            YANDEX_MONEY -> when (InstrumentType.parse(jsonObject.optString("instrument_type"))) {
                WALLET -> Wallet(
                    id = id,
                    charge = charge,
                    fee = null,
                    walletId = jsonObject.optString("id"),
                    balance = jsonObject.optJSONObject("balance").toAmount(),
                    userName = userName
                )
                LINKED_BANK_CARD -> LinkedCard(
                    id = id,
                    charge = charge,
                    fee = null,
                    cardId = jsonObject.getString("id"),
                    name = jsonObject.optString("card_name"),
                    pan = jsonObject.getString("card_mask"),
                    brand = jsonObject.getCardBrand()
                )
                UNKNOWN -> AbstractWallet(id, charge, null)
            }
        }
    }
}

internal enum class InstrumentType(val type: String) {
    WALLET("wallet"),
    LINKED_BANK_CARD("linked_bank_card"),
    UNKNOWN("unknown");

    companion object {
        fun parse(type: String?) = values().find { it.type == type } ?: UNKNOWN
    }
}
