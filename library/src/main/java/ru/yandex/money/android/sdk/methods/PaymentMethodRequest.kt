/*
 * The MIT License (MIT)
 * Copyright © 2019 NBCO Yandex.Money LLC
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
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.impl.extensions.toPaymentMethodResponse
import ru.yandex.money.android.sdk.methods.base.GetRequest
import ru.yandex.money.android.sdk.model.Error
import ru.yandex.money.android.sdk.model.PaymentMethodBankCard

private const val PAYMENT_METHOD_PATH = "/payment_method"
private const val PAYMENT_METHOD_PARAM_NAME = "payment_method_id"

internal class PaymentMethodRequest(
    private val paymentMethodId: String,
    private val shopToken: String,
    private val userAuthToken: String?
) : GetRequest<PaymentMethodResponse> {

    override fun getUrl(): String {
        return BuildConfig.HOST + PAYMENT_METHOD_PATH + "?$PAYMENT_METHOD_PARAM_NAME=" + paymentMethodId
    }

    override fun getHeaders(): List<Pair<String, String>> {
        val headers = mutableListOf("Authorization" to Credentials.basic(shopToken, ""))
        userAuthToken?.also {
            headers += "Passport-Authorization" to userAuthToken
        }
        return headers
    }

    override fun convertJsonToResponse(jsonObject: JSONObject): PaymentMethodResponse {
        return jsonObject.toPaymentMethodResponse()
    }
}

internal data class PaymentMethodResponse(
    val paymentMethodBankCard: PaymentMethodBankCard?,
    val error: Error?
)