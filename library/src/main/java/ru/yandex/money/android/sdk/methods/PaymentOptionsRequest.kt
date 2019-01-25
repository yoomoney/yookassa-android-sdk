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

package ru.yandex.money.android.sdk.methods

import okhttp3.Credentials
import org.json.JSONObject
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.impl.extensions.toPaymentOptionResponse
import ru.yandex.money.android.sdk.methods.base.GetRequest
import ru.yandex.money.android.sdk.model.AnonymousUser
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.model.CurrentUser
import ru.yandex.money.android.sdk.model.Error
import ru.yandex.money.android.sdk.model.PaymentOption

private const val PAYMENT_OPTIONS_METHOD_PATH = "/payment_options"
private const val AMOUNT = "amount"
private const val CURRENCY = "currency"
private const val GATEWAY_ID = "gateway_id"

internal data class PaymentOptionsRequest(
    private val amount: Amount,
    private val currentUser: CurrentUser,
    private val gatewayId: String?,
    private val userAuthToken: String?,
    private val shopToken: String
) : GetRequest<PaymentOptionsResponse> {

    override fun getHeaders(): List<Pair<String, String>> {
        val headers = mutableListOf("Authorization" to Credentials.basic(shopToken, ""))
        userAuthToken?.also {
            headers += "Passport-Authorization" to userAuthToken
        }
        return headers
    }

    override fun getUrl(): String {
        val map = mutableMapOf<String, String>()
        map[AMOUNT] = amount.value.toString()
        map[CURRENCY] = amount.currency.toString()
        if (gatewayId != null) {
            map[GATEWAY_ID] = gatewayId
        }

        return getHost() + createFullPath(PAYMENT_OPTIONS_METHOD_PATH, map)
    }

    private fun getHost() = BuildConfig.HOST

    private fun createFullPath(path: String, params: Map<String, String>) =
            path
                    .takeIf { params.isNotEmpty() }
                    ?.plus(params
                            .map { "${it.key}=${it.value}" }
                            .joinToString(prefix = "?", separator = "&"))
                    ?: path

    override fun convertJsonToResponse(jsonObject: JSONObject): PaymentOptionsResponse {
        val userName = when (currentUser) {
            is AuthorizedUser -> currentUser.userName
            AnonymousUser -> ""
        }
        return jsonObject.toPaymentOptionResponse(userName)
    }
}

internal data class PaymentOptionsResponse(
    val paymentOptions: List<PaymentOption>,
    val error: Error?
)
