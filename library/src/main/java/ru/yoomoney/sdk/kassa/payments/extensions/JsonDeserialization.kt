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
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.payment.paymentOptionFactory
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutAuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.CardInfo
import ru.yoomoney.sdk.kassa.payments.model.Error
import ru.yoomoney.sdk.kassa.payments.model.ExtendedStatus
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.Status
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

internal fun JSONObject.toPaymentOptionResponse(): Result<List<PaymentOption>> =
    if (optString("type") == "error") {
        Result.Fail(ApiMethodException(toError()))
    } else {
        Result.Success(
            getJSONArray("items")
                .mapIndexed { id, jsonObject ->
                    paymentOptionFactory(id, jsonObject)
                }
                .filterNotNull()
        )
    }

internal fun JSONObject.toPaymentMethodResponse(): Result<PaymentMethodBankCard> =
    if (optString("type") == "error") {
        Result.Fail(ApiMethodException(toError()))
    } else {
        val cardJson = getJSONObject("card")
        Result.Success(
            PaymentMethodBankCard(
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
            )
        )
    }

internal fun JSONObject.toTokenResponse(): Result<String> =
    if (optString("type") == "error") {
        Result.Fail(ApiMethodException(toError()))
    } else {
        Result.Success(getString("payment_token"))
    }

internal fun JSONObject.toAuthContextGetResponse(): Result<CheckoutAuthContextGetResponse> {
    return when (getStatus()) {
        Status.SUCCESS -> Result.Success(
            CheckoutAuthContextGetResponse(
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
        )
        Status.REFUSED -> Result.Fail(ApiMethodException(optString("error").toErrorCode()))
        Status.UNKNOWN -> Result.Fail(
            ApiMethodException(
                getJSONObject("error").getString("type").toErrorCode()
            )
        )
    }
}

internal fun JSONObject.toCheckoutTokenIssueInitResponse(): Result<CheckoutTokenIssueInitResponse> {
    return when (getExtendedStatus()) {
        ExtendedStatus.SUCCESS -> Result.Success(CheckoutTokenIssueInitResponse.Success(
            getJSONObject("result").getString("processId")
        ))
        ExtendedStatus.REFUSED -> Result.Fail(
            ApiMethodException(
                optString("error").toErrorCode()
            )
        )
        ExtendedStatus.AUTH_REQUIRED -> {
            val result = getJSONObject("result")
            Result.Success(CheckoutTokenIssueInitResponse.AuthRequired(
                authContextId = result.optString("authContextId"),
                processId = result.optString("processId")
            ))
        }
        ExtendedStatus.UNKNOWN -> Result.Fail(
            ApiMethodException(
                getJSONObject("error").getString("type").toErrorCode()
            )
        )
    }
}

internal fun JSONObject.toAuthCheckResponse(): Result<Unit> {
    return when (getStatus()) {
        Status.SUCCESS -> Result.Success(Unit)
        Status.REFUSED -> Result.Fail(
            AuthCheckApiMethodException(
                Error(getString("error").toErrorCode()),
                optJSONObject("result")?.toAuthTypeState()
            )
        )
        Status.UNKNOWN -> Result.Fail(
            ApiMethodException(
                getJSONObject("error").getString("type").toErrorCode()
            )
        )
    }
}

internal fun JSONObject.toAuthSessionGenerateResponse(): Result<AuthTypeState> {
    return when (getStatus()) {
        Status.SUCCESS -> Result.Success(getJSONObject("result").toAuthTypeState())
        Status.REFUSED -> Result.Fail(ApiMethodException(optString("error").toErrorCode()))
        Status.UNKNOWN -> Result.Fail(
            ApiMethodException(
                getJSONObject("error").getString("type").toErrorCode()
            )
        )
    }
}

internal fun JSONObject.toCheckoutTokenIssueExecuteResponse(): Result<String> {
    return when(getStatus()) {
        Status.SUCCESS -> Result.Success(getJSONObject("result").getString("accessToken"))
        Status.REFUSED -> Result.Fail(ApiMethodException(optString("error").toErrorCode()))
        Status.UNKNOWN -> Result.Fail(
            ApiMethodException(
                getJSONObject("error").getString("type").toErrorCode()
            )
        )
    }
}

internal fun JSONObject.toAuthTypeState(): AuthTypeState {
    return when (getAuthType("type")) {
        AuthType.UNKNOWN -> AuthTypeState.NotRequired
        AuthType.PUSH -> AuthTypeState.Push
        AuthType.EMERGENCY -> AuthTypeState.Emergency
        AuthType.OAUTH_TOKEN -> AuthTypeState.OauthToken
        AuthType.SECURE_PASSWORD -> AuthTypeState.SecurePassword
        AuthType.TOTP -> AuthTypeState.TOTP
        AuthType.SMS -> AuthTypeState.SMS(
            nextSessionTimeLeft = optInt("nextSessionTimeLeft"),
            codeLength = optInt("codeLength"),
            attemptsCount = optInt("attemptsCount"),
            attemptsLeft = optInt("attemptsLeft")
        )
        else -> AuthTypeState.UNKNOWN
    }
}

internal fun JSONObject.getPaymentMethodType(name: String) = when (optString(name)) {
    "bank_card" -> PaymentMethodType.BANK_CARD
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
