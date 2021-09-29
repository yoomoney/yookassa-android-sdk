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

package ru.yoomoney.sdk.kassa.payments.methods

import okhttp3.Credentials
import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.extensions.toPaymentOptionResponse
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.methods.base.GetRequest
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionsResponse
import ru.yoomoney.sdk.kassa.payments.model.Result

private const val PAYMENT_OPTIONS_METHOD_PATH = "/payment_options"
private const val AMOUNT = "amount"
private const val CURRENCY = "currency"
private const val GATEWAY_ID = "gateway_id"
private const val SAVE_PAYMENT_METHOD = "save_payment_method"

internal data class PaymentOptionsRequest(
    private val hostProvider: HostProvider,
    private val configRepository: ConfigRepository,
    private val amount: Amount,
    private val currentUser: CurrentUser,
    private val gatewayId: String?,
    private val userAuthToken: String?,
    private val shopToken: String,
    private val savePaymentMethod: SavePaymentMethod,
    private val merchantCustomerId: String?
) : GetRequest<Result<PaymentOptionsResponse>> {

    override fun getHeaders(): List<Pair<String, String>> {
        val headers = mutableListOf("Authorization" to Credentials.basic(shopToken, ""))
        userAuthToken?.also {
            headers += "Passport-Authorization" to userAuthToken
        }
        return headers
    }

    override fun getUrl(): String {
        val params = mutableMapOf<String, String>()
        params[AMOUNT] = amount.value.toString()
        params[CURRENCY] = amount.currency.toString()
        if (gatewayId != null) {
            params[GATEWAY_ID] = gatewayId
        }
        merchantCustomerId?.let {
            params["merchant_customer_id"] = merchantCustomerId
        }

        when (savePaymentMethod) {
            SavePaymentMethod.ON -> params[SAVE_PAYMENT_METHOD] = "true"
            SavePaymentMethod.OFF -> params[SAVE_PAYMENT_METHOD] = "false"
            SavePaymentMethod.USER_SELECTS -> {
                //don't add SAVE_PAYMENT_METHOD param
            }
        }.toString()

        return hostProvider.host() + createFullPath(PAYMENT_OPTIONS_METHOD_PATH, params)
    }

    private fun createFullPath(path: String, params: Map<String, String>) =
        path.takeIf { params.isNotEmpty() }
            ?.plus(params.map { "${it.key}=${it.value}" }
                .joinToString(prefix = "?", separator = "&"))
            ?: path

    override fun convertJsonToResponse(jsonObject: JSONObject): Result<PaymentOptionsResponse> {
        return jsonObject.toPaymentOptionResponse(configRepository.getConfig().paymentMethods)
    }
}
