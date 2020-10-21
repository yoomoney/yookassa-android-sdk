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
import org.json.JSONStringer
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.model.AuthType
import ru.yandex.money.android.sdk.model.AuthType.EMERGENCY
import ru.yandex.money.android.sdk.model.AuthType.NOT_NEEDED
import ru.yandex.money.android.sdk.model.AuthType.OAUTH_TOKEN
import ru.yandex.money.android.sdk.model.AuthType.PUSH
import ru.yandex.money.android.sdk.model.AuthType.SECURE_PASSWORD
import ru.yandex.money.android.sdk.model.AuthType.SMS
import ru.yandex.money.android.sdk.model.AuthType.TOTP
import ru.yandex.money.android.sdk.model.AuthType.UNKNOWN
import ru.yandex.money.android.sdk.model.Confirmation
import ru.yandex.money.android.sdk.model.ExternalConfirmation
import ru.yandex.money.android.sdk.model.GooglePayInfo
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.LinkedCardInfo
import ru.yandex.money.android.sdk.model.NewCardInfo
import ru.yandex.money.android.sdk.model.NoConfirmation
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.PaymentOptionInfo
import ru.yandex.money.android.sdk.model.RedirectConfirmation
import ru.yandex.money.android.sdk.model.SbolSmsInvoicingInfo
import ru.yandex.money.android.sdk.model.Wallet
import ru.yandex.money.android.sdk.model.WalletInfo

internal fun Amount.toJsonObject(): JSONObject = JSONObject()
    .put("value", value.toString())
    .put("currency", currency)

internal fun PaymentMethodType.toJsonString(): String = when(this) {
    PaymentMethodType.YANDEX_MONEY -> "yandex_money"
    PaymentMethodType.BANK_CARD -> "bank_card"
    PaymentMethodType.YOO_MONEY -> "yoo_money"
    PaymentMethodType.GOOGLE_PAY -> "google_pay"
    PaymentMethodType.SBERBANK -> "sberbank"
}

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
        .put("id", (paymentOption as Wallet).walletId)
        .put("type", paymentOption.paymentMethodType.toJsonString())
        .put("instrument_type", "wallet")
        .put("balance", paymentOption.balance.toJsonObject())
    is LinkedCardInfo -> JSONObject()
        .put("id", (paymentOption as LinkedCard).cardId)
        .put("type", paymentOption.paymentMethodType.toJsonString())
        .put("instrument_type", "linked_bank_card")
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

internal fun Confirmation.toJsonObject(): JSONObject? = when (this) {
    NoConfirmation -> null
    ExternalConfirmation -> JSONObject()
        .put("type", "external")
    is RedirectConfirmation -> JSONObject()
        .put("type", "redirect")
        .put("return_url", returnUrl)
}
