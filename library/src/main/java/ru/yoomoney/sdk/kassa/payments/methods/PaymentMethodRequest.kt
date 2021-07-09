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
import ru.yoomoney.sdk.kassa.payments.extensions.toPaymentMethodResponse
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.methods.base.GetRequest
import ru.yoomoney.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoomoney.sdk.kassa.payments.model.Result

private const val PAYMENT_METHOD_PATH = "/payment_method"
private const val PAYMENT_METHOD_PARAM_NAME = "payment_method_id"

internal class PaymentMethodRequest(
    private val hostProvider: HostProvider,
    private val paymentMethodId: String,
    private val shopToken: String,
    private val userAuthToken: String?
) : GetRequest<Result<PaymentMethodBankCard>> {

    override fun getUrl(): String {
        return hostProvider.host() + PAYMENT_METHOD_PATH + "?$PAYMENT_METHOD_PARAM_NAME=" + paymentMethodId
    }

    override fun getHeaders(): List<Pair<String, String>> {
        val headers = mutableListOf("Authorization" to Credentials.basic(shopToken, ""))
        userAuthToken?.also {
            headers += "Passport-Authorization" to userAuthToken
        }
        return headers
    }

    override fun convertJsonToResponse(jsonObject: JSONObject): Result<PaymentMethodBankCard> {
        return jsonObject.toPaymentMethodResponse()
    }
}