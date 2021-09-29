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

package ru.yoomoney.sdk.kassa.payments.model

import org.json.JSONArray
import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.extensions.mapIndexed

private const val YOO_MONEY_LOGO_URL_LIGHT_FIELD = "yooMoneyLogoUrlLight"
private const val YOO_MONEY_LOGO_URL_DARK_FIELD = "yooMoneyLogoUrlDark"
private const val USER_AGREEMENT_URL_FIELD = "userAgreementUrl"
private const val GATEWAY_FIELD = "googlePayGateway"
private const val YOO_MONEY_API_ENDPOINT_FIELD = "yooMoneyApiEndpoint"
private const val YOO_MONEY_PAYMENT_AUTHORIZATION_ENDPOINT_FIELD = "yooMoneyPaymentAuthorizationApiEndpoint"
private const val YOO_MONEY_AUTH_API_ENDPOINT_FIELD = "yooMoneyAuthApiEndpoint"
private const val PAYMENT_METHODS_FIELD = "paymentMethods"
private const val SAVE_PAYMENT_OPTION_TEXTS_FIELD = "savePaymentMethodOptionTexts"

internal data class Config(
    val yooMoneyLogoUrlLight: String,
    val yooMoneyLogoUrlDark: String,
    val paymentMethods: List<ConfigPaymentOption>,
    val savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    val userAgreementUrl: String,
    val gateway: String,
    val yooMoneyApiEndpoint: String,
    val yooMoneyPaymentAuthorizationApiEndpoint: String,
    val yooMoneyAuthApiEndpoint: String?
)

internal fun Config.toJsonObject() = JSONObject().apply {
    put(YOO_MONEY_LOGO_URL_LIGHT_FIELD, yooMoneyLogoUrlLight)
    put(YOO_MONEY_LOGO_URL_DARK_FIELD, yooMoneyLogoUrlDark)
    put(USER_AGREEMENT_URL_FIELD, userAgreementUrl)
    put(GATEWAY_FIELD, gateway)
    put(YOO_MONEY_API_ENDPOINT_FIELD, yooMoneyApiEndpoint)
    put(PAYMENT_METHODS_FIELD, JSONArray().apply {
        paymentMethods.forEach {
            put(it.toJsonObject())
        }
    })
    put(SAVE_PAYMENT_OPTION_TEXTS_FIELD, JSONObject().apply {
        put("switchRecurrentOnBindOnTitle", savePaymentMethodOptionTexts.switchRecurrentOnBindOnTitle)
        put("switchRecurrentOnBindOnSubtitle", savePaymentMethodOptionTexts.switchRecurrentOnBindOnSubtitle)
        put("switchRecurrentOnBindOffTitle", savePaymentMethodOptionTexts.switchRecurrentOnBindOffTitle)
        put("switchRecurrentOnBindOffSubtitle", savePaymentMethodOptionTexts.switchRecurrentOnBindOffSubtitle)
        put("switchRecurrentOffBindOnTitle", savePaymentMethodOptionTexts.switchRecurrentOffBindOnTitle)
        put("switchRecurrentOffBindOnSubtitle", savePaymentMethodOptionTexts.switchRecurrentOffBindOnSubtitle)
        put("messageRecurrentOnBindOnTitle", savePaymentMethodOptionTexts.messageRecurrentOnBindOnTitle)
        put("messageRecurrentOnBindOnSubtitle", savePaymentMethodOptionTexts.messageRecurrentOnBindOnSubtitle)
        put("messageRecurrentOnBindOffTitle", savePaymentMethodOptionTexts.messageRecurrentOnBindOffTitle)
        put("messageRecurrentOnBindOffSubtitle", savePaymentMethodOptionTexts.messageRecurrentOnBindOffSubtitle)
        put("messageRecurrentOffBindOnTitle", savePaymentMethodOptionTexts.messageRecurrentOffBindOnTitle)
        put("messageRecurrentOffBindOnSubtitle", savePaymentMethodOptionTexts.messageRecurrentOffBindOnSubtitle)
        put("screenRecurrentOnBindOnTitle", savePaymentMethodOptionTexts.screenRecurrentOnBindOnTitle)
        put("screenRecurrentOnBindOnText", savePaymentMethodOptionTexts.screenRecurrentOnBindOnText)
        put("screenRecurrentOnBindOffTitle", savePaymentMethodOptionTexts.screenRecurrentOnBindOffTitle)
        put("screenRecurrentOnBindOffText", savePaymentMethodOptionTexts.screenRecurrentOnBindOffText)
        put("screenRecurrentOffBindOnTitle", savePaymentMethodOptionTexts.screenRecurrentOffBindOnTitle)
        put("screenRecurrentOffBindOnText", savePaymentMethodOptionTexts.screenRecurrentOffBindOnText)
        put("screenRecurrentOnSberpayTitle", savePaymentMethodOptionTexts.screenRecurrentOnSberpayTitle)
        put("screenRecurrentOnSberpayText", savePaymentMethodOptionTexts.screenRecurrentOnSberpayText)
    })
    put(YOO_MONEY_PAYMENT_AUTHORIZATION_ENDPOINT_FIELD, yooMoneyPaymentAuthorizationApiEndpoint)
    put(YOO_MONEY_AUTH_API_ENDPOINT_FIELD, yooMoneyAuthApiEndpoint)
}

internal fun JSONObject.toConfig(): Config = Config(
    yooMoneyLogoUrlLight = getString(YOO_MONEY_LOGO_URL_LIGHT_FIELD),
    yooMoneyLogoUrlDark = getString(YOO_MONEY_LOGO_URL_DARK_FIELD),
    userAgreementUrl = getString(USER_AGREEMENT_URL_FIELD),
    gateway = getString(GATEWAY_FIELD),
    yooMoneyApiEndpoint = getString(YOO_MONEY_API_ENDPOINT_FIELD),
    yooMoneyPaymentAuthorizationApiEndpoint = getString(YOO_MONEY_PAYMENT_AUTHORIZATION_ENDPOINT_FIELD),
    yooMoneyAuthApiEndpoint = getString(YOO_MONEY_AUTH_API_ENDPOINT_FIELD),
    paymentMethods = getJSONArray(PAYMENT_METHODS_FIELD).mapIndexed { i, jsonObject ->
        jsonObject.toConfigPaymentOption()
    },
    savePaymentMethodOptionTexts = getJSONObject("savePaymentMethodOptionTexts").let {
        SavePaymentMethodOptionTexts(
            switchRecurrentOnBindOnTitle = it.getString("switchRecurrentOnBindOnTitle"),
            switchRecurrentOnBindOnSubtitle = it.getString("switchRecurrentOnBindOnSubtitle"),
            switchRecurrentOnBindOffTitle = it.getString("switchRecurrentOnBindOffTitle"),
            switchRecurrentOnBindOffSubtitle = it.getString("switchRecurrentOnBindOffSubtitle"),
            switchRecurrentOffBindOnTitle = it.getString("switchRecurrentOffBindOnTitle"),
            switchRecurrentOffBindOnSubtitle = it.getString("switchRecurrentOffBindOnSubtitle"),
            messageRecurrentOnBindOnTitle = it.getString("messageRecurrentOnBindOnTitle"),
            messageRecurrentOnBindOnSubtitle = it.getString("messageRecurrentOnBindOnSubtitle"),
            messageRecurrentOnBindOffTitle = it.getString("messageRecurrentOnBindOffTitle"),
            messageRecurrentOnBindOffSubtitle = it.getString("messageRecurrentOnBindOffSubtitle"),
            messageRecurrentOffBindOnTitle = it.getString("messageRecurrentOffBindOnTitle"),
            messageRecurrentOffBindOnSubtitle = it.getString("messageRecurrentOffBindOnSubtitle"),
            screenRecurrentOnBindOnTitle = it.getString("screenRecurrentOnBindOnTitle"),
            screenRecurrentOnBindOnText = it.getString("screenRecurrentOnBindOnText"),
            screenRecurrentOnBindOffTitle = it.getString("screenRecurrentOnBindOffTitle"),
            screenRecurrentOnBindOffText = it.getString("screenRecurrentOnBindOffText"),
            screenRecurrentOffBindOnTitle = it.getString("screenRecurrentOffBindOnTitle"),
            screenRecurrentOffBindOnText = it.getString("screenRecurrentOffBindOnText"),
            screenRecurrentOnSberpayTitle = it.getString("screenRecurrentOnSberpayTitle"),
            screenRecurrentOnSberpayText = it.getString("screenRecurrentOnSberpayText")
        )
    }
)