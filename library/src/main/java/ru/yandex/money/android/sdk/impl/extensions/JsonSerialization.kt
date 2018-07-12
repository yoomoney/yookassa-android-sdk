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

package ru.yandex.money.android.sdk.impl.extensions

import org.json.JSONObject
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.AuthType
import ru.yandex.money.android.sdk.AuthType.EMERGENCY
import ru.yandex.money.android.sdk.AuthType.NOT_NEEDED
import ru.yandex.money.android.sdk.AuthType.OAUTH_TOKEN
import ru.yandex.money.android.sdk.AuthType.PUSH
import ru.yandex.money.android.sdk.AuthType.SECURE_PASSWORD
import ru.yandex.money.android.sdk.AuthType.SMS
import ru.yandex.money.android.sdk.AuthType.TOTP
import ru.yandex.money.android.sdk.AuthType.UNKNOWN
import ru.yandex.money.android.sdk.GooglePayInfo
import ru.yandex.money.android.sdk.LinkedCard
import ru.yandex.money.android.sdk.LinkedCardInfo
import ru.yandex.money.android.sdk.NewCardInfo
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.PaymentOptionInfo
import ru.yandex.money.android.sdk.SbolSmsInvoicingInfo
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.WalletInfo

internal fun Amount.toJsonObject(): JSONObject = JSONObject()
    .put("value", value.toString())
    .put("currency", currency)

internal fun PaymentOptionInfo.toJsonObject(paymentOption: PaymentOption): JSONObject = when (this) {
    is NewCardInfo -> JSONObject()
        .put("type", "bank_card")
        .put(
            "card", JSONObject()
                .put("number", number)
                .putOpt("expiry_year", expirationYear)
                .putOpt("expiry_month", expirationMonth)
                .put("csc", csc)
        )
    is WalletInfo -> JSONObject()
        .put("type", "yandex_money")
        .put("instrument_type", "wallet")
        .put("id", (paymentOption as Wallet).walletId)
        .put("balance", paymentOption.balance.toJsonObject())
    is LinkedCardInfo -> JSONObject()
        .put("type", "yandex_money")
        .put("instrument_type", "linked_bank_card")
        .put("id", (paymentOption as LinkedCard).cardId)
        .put("csc", this.csc)
    is SbolSmsInvoicingInfo -> JSONObject()
        .put("type", "sberbank")
        .put("phone", phone.filter(Char::isDigit))
    is GooglePayInfo -> JSONObject()
        .put("type", "google_pay")
        .put("payment_method_token", paymentMethodToken)
        .put("google_transaction_id", googleTransactionId)
}

internal fun AuthType.toJsonString() = when (this) {
    SMS -> "Sms"
    TOTP -> "Totp"
    SECURE_PASSWORD -> "Password"
    EMERGENCY -> "Emergency"
    PUSH -> "Push"
    OAUTH_TOKEN -> "OauthToken"
    NOT_NEEDED, UNKNOWN -> ""
}
