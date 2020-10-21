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
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.impl.payment.paymentOptionFactory
import ru.yandex.money.android.sdk.methods.PaymentMethodResponse
import ru.yandex.money.android.sdk.methods.PaymentOptionsResponse
import ru.yandex.money.android.sdk.methods.TokenResponse
import ru.yandex.money.android.sdk.methods.WalletCheckResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthCheckResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthContextGetResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthSessionGenerateResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueExecuteResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yandex.money.android.sdk.model.AuthType
import ru.yandex.money.android.sdk.model.AuthTypeState
import ru.yandex.money.android.sdk.model.CardBrand
import ru.yandex.money.android.sdk.model.CardInfo
import ru.yandex.money.android.sdk.model.Error
import ru.yandex.money.android.sdk.model.ExtendedStatus
import ru.yandex.money.android.sdk.model.Fee
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.LinkedCardInfo
import ru.yandex.money.android.sdk.model.PaymentMethodBankCard
import ru.yandex.money.android.sdk.model.Status
import java.math.BigDecimal
import java.util.Currency

internal fun JSONObject.toAmount() = Amount(
    value = BigDecimal(get("value").toString()),
    currency = Currency.getInstance(get("currency").toString())
)

internal fun JSONObject.getFee(): Fee? {
    return optJSONObject("fee")?.run {
        Fee(
            service = optJSONObject("service")?.toAmount(),
            counterparty = optJSONObject("counterparty")?.toAmount()
        )
    }
}

internal fun JSONObject.toError() = Error(
    errorCode = getString("code").toErrorCode(),
    id = optString("id"),
    description = optString("description"),
    parameter = optString("parameter"),
    retryAfter = optString("retry_after").toIntOrNull() ?: 0
)

internal fun JSONObject.toPaymentOptionResponse(): PaymentOptionsResponse =
    if (optString("type") == "error") {
        PaymentOptionsResponse(
            paymentOptions = listOf(),
            error = toError()
        )
    } else {
        PaymentOptionsResponse(
            paymentOptions = getJSONArray("items")
                .mapIndexed { id, jsonObject ->
                    paymentOptionFactory(id, jsonObject)
                }
                .filterNotNull(),
            error = null
        )
    }

internal fun JSONObject.toPaymentMethodResponse(): PaymentMethodResponse =
    if (optString("type") == "error") {
        PaymentMethodResponse(
            paymentMethodBankCard = null,
            error = toError()
        )
    } else {
        val cardJson = getJSONObject("card")
        PaymentMethodResponse(
            paymentMethodBankCard = PaymentMethodBankCard(
                type = getPaymentMethodType("type") ?: PaymentMethodType.BANK_CARD,
                id = getString("id"),
                saved = getBoolean("saved"),
                cscRequired = getBoolean("csc_required"),
                title = optString("title", null),
                card = CardInfo(
                    first = cardJson.getString("first6"),
                    last = cardJson.getString("last4"),
                    expiryYear = cardJson.getString("expiry_year"),
                    expiryMonth = cardJson.getString("expiry_month"),
                    cardType = cardJson.getCardBrand(),
                    source = cardJson.getPaymentMethodType("source") ?: PaymentMethodType.GOOGLE_PAY
                )
            ),
            error = null
        )
    }

internal fun JSONObject.toTokenResponse(): TokenResponse =
    if (optString("type") == "error") {
        TokenResponse(
            paymentToken = null,
            error = toError()
        )
    } else {
        TokenResponse(
            paymentToken = getString("payment_token"),
            error = null
        )
    }

internal fun JSONObject.toAuthContextGetResponse(): CheckoutAuthContextGetResponse {
    val status = getStatus()
    return CheckoutAuthContextGetResponse(
        status = status,
        errorCode = when (status) {
            Status.SUCCESS -> null
            Status.REFUSED -> optString("error").toErrorCode()
            Status.UNKNOWN -> getJSONObject("error").getString("type").toErrorCode()
        },
        authTypeStates = optJSONObject("result")
            ?.optJSONArray("authTypes")
            ?.let { array ->
                (0 until array.length())
                    .asSequence()
                    .map(array::getJSONObject)
                    .filter { it.getBoolean("enabled") }
                    .map(JSONObject::toAuthTypeState)
                    .toList()
                    .toTypedArray()
            } ?: emptyArray(),
        defaultAuthType = optJSONObject("result")?.getAuthType("defaultAuthType") ?: AuthType.UNKNOWN
    )
}

internal fun JSONObject.toCheckoutTokenIssueInitResponse(): CheckoutTokenIssueInitResponse {
    val status = getExtendedStatus()
    val result: JSONObject? = optJSONObject("result")
    return CheckoutTokenIssueInitResponse(
        status = status,
        errorCode = when (status) {
            ExtendedStatus.SUCCESS -> null
            ExtendedStatus.REFUSED -> optString("error").toErrorCode()
            ExtendedStatus.AUTH_REQUIRED -> null
            ExtendedStatus.UNKNOWN -> getJSONObject("error").getString("type").toErrorCode()
        },
        authContextId = result?.optString("authContextId"),
        processId = result?.optString("processId")
    )
}

internal fun JSONObject.toAuthCheckResponse(): CheckoutAuthCheckResponse {
    val status = getStatus()
    return CheckoutAuthCheckResponse(
        status = status,
        errorCode = when (status) {
            Status.SUCCESS -> null
            Status.REFUSED -> optString("error").toErrorCode()
            Status.UNKNOWN -> getJSONObject("error").getString("type").toErrorCode()
        },
        authTypeState = optJSONObject("result")?.toAuthTypeState()
    )
}

internal fun JSONObject.toAuthSessionGenerateResponse(): CheckoutAuthSessionGenerateResponse {
    val status = getStatus()
    return CheckoutAuthSessionGenerateResponse(
        status = status,
        errorCode = when (status) {
            Status.SUCCESS -> null
            Status.REFUSED -> optString("error").toErrorCode()
            Status.UNKNOWN -> getJSONObject("error").getString("type").toErrorCode()
        },
        authTypeState = optJSONObject("result")?.toAuthTypeState()
    )
}

internal fun JSONObject.toCheckoutTokenIssueExecuteResponse(): CheckoutTokenIssueExecuteResponse {
    val status = getStatus()
    return CheckoutTokenIssueExecuteResponse(
        status = status,
        errorCode = when (status) {
            Status.SUCCESS -> null
            Status.REFUSED -> optString("error").toErrorCode()
            Status.UNKNOWN -> getJSONObject("error").getString("type").toErrorCode()
        },
        accessToken = optJSONObject("result")?.getString("accessToken")
    )
}

internal fun JSONObject.toAuthTypeState() = AuthTypeState(
    type = getAuthType("type"),
    nextSessionTimeLeft = optInt("nextSessionTimeLeft")
)

internal fun JSONObject.getPaymentMethodType(name: String) = when (optString(name)) {
    "bank_card" -> PaymentMethodType.BANK_CARD
    "yandex_money" -> PaymentMethodType.YANDEX_MONEY
    "yoo_money" -> PaymentMethodType.YOO_MONEY
    "sberbank" -> PaymentMethodType.SBERBANK
    "google_pay" -> PaymentMethodType.GOOGLE_PAY
    else -> null
}

internal fun JSONObject.getCardBrand() = when (getString("card_type")) {
    "MasterCard" -> CardBrand.MASTER_CARD
    "Visa" -> CardBrand.VISA
    "MIR" -> CardBrand.MIR
    "AmericanExpress" -> CardBrand.AMERICAN_EXPRESS
    "JCB" -> CardBrand.JCB
    "CUP" -> CardBrand.CUP
    "DinersClub" -> CardBrand.DINERS_CLUB
    "BankCard" -> CardBrand.BANK_CARD
    "DiscoverCard" -> CardBrand.DISCOVER_CARD
    "InstaPayment" -> CardBrand.INSTA_PAYMENT
    "InstaPaymentTM" -> CardBrand.INSTA_PAYMENT_TM
    "Laser" -> CardBrand.LASER
    "Dankort" -> CardBrand.DANKORT
    "Solo" -> CardBrand.SOLO
    "Switch" -> CardBrand.SWITCH
    "Unknown" -> CardBrand.UNKNOWN
    else -> CardBrand.UNKNOWN
}

internal fun JSONObject.getAuthType(value: String) = when (getString(value)) {
    "Sms" -> AuthType.SMS
    "Totp" -> AuthType.TOTP
    "Password" -> AuthType.SECURE_PASSWORD
    "Emergency" -> AuthType.EMERGENCY
    "Push" -> AuthType.PUSH
    "OauthToken" -> AuthType.OAUTH_TOKEN
    else -> AuthType.UNKNOWN
}

internal fun JSONObject.getStatus() = when (optString("status")) {
    "Success" -> Status.SUCCESS
    "Refused" -> Status.REFUSED
    else -> Status.UNKNOWN
}

internal fun JSONObject.getExtendedStatus() = when (optString("status")) {
    "Success" -> ExtendedStatus.SUCCESS
    "Refused" -> ExtendedStatus.REFUSED
    "AuthRequired" -> ExtendedStatus.AUTH_REQUIRED
    else -> ExtendedStatus.UNKNOWN
}

internal fun JSONObject.getDisplayName(): String = getString("display_name")

internal fun JSONObject.toWalletCheckResponse() = when {
    has("account_number") -> WalletCheckResponse(Status.SUCCESS, true)
    has("terms_and_conditions_apply_required") -> WalletCheckResponse(Status.SUCCESS, false)
    else -> WalletCheckResponse(Status.REFUSED, null)
}
