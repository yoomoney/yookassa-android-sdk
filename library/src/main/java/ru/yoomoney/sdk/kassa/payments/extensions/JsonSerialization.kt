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

package ru.yoomoney.sdk.kassa.payments.extensions

import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthType.EMERGENCY
import ru.yoomoney.sdk.kassa.payments.model.AuthType.NOT_NEEDED
import ru.yoomoney.sdk.kassa.payments.model.AuthType.OAUTH_TOKEN
import ru.yoomoney.sdk.kassa.payments.model.AuthType.PUSH
import ru.yoomoney.sdk.kassa.payments.model.AuthType.SECURE_PASSWORD
import ru.yoomoney.sdk.kassa.payments.model.AuthType.SMS
import ru.yoomoney.sdk.kassa.payments.model.AuthType.TOTP
import ru.yoomoney.sdk.kassa.payments.model.AuthType.UNKNOWN
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePayInfo
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoomoney.sdk.kassa.payments.model.SberPay
import ru.yoomoney.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.WalletInfo

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
        .put("id", (paymentOption as Wallet).walletId)
        .put("type", "yoo_money")
        .put("instrument_type", "wallet")
        .put("balance", paymentOption.balance.toJsonObject())
    is LinkedCardInfo -> JSONObject()
        .put("id", (paymentOption as LinkedCard).cardId)
        .put("type", "yoo_money")
        .put("instrument_type", "linked_bank_card")
        .put("csc", this.csc)
    is SbolSmsInvoicingInfo -> JSONObject()
        .put("type", "sberbank")
        .put("phone", phone.filter(Char::isDigit))
    is SberPay -> JSONObject()
        .put("type", "sberbank")
    is GooglePayInfo -> JSONObject()
        .put("type", "google_pay")
        .put("payment_method_token", paymentMethodToken)
        .put("google_transaction_id", googleTransactionId)
    SberPay -> JSONObject()
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
    is MobileApplication -> JSONObject()
        .put("type", "mobile_application")
        .put("return_url", returnUrl)
        .put("app_os_type", "android")
}
