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

package ru.yoomoney.sdk.kassa.payments.methods.unbindCard

import okhttp3.Credentials
import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.extensions.toUnbindingResponse
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.methods.base.DeleteRequest
import ru.yoomoney.sdk.kassa.payments.methods.base.MimeType
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SuccessUnbinding

private const val UNTIE_CARD_METHOD_PATH = "/payment_instruments"

internal class UnbindCardMethodRequest(
    private val hostProvider: HostProvider,
    private val paymentMethodId: String,
    private val shopToken: String,
    private val paymentAuthToken: String?
) : DeleteRequest<Result<SuccessUnbinding>> {

    override fun getUrl(): String {
        return hostProvider.host() + "$UNTIE_CARD_METHOD_PATH/$paymentMethodId"
    }

    override fun getPayload(): List<Pair<String, Any>> = emptyList()

    override fun getHeaders() = listOf("Authorization" to Credentials.basic(shopToken, "")).let { headers ->
        headers.takeIf { paymentAuthTokenPresent() }?.plus("Wallet-Authorization" to "Bearer $paymentAuthToken")
            ?: headers
    }

    override fun convertJsonToResponse(jsonObject: JSONObject): Result<SuccessUnbinding> {
        return jsonObject.toUnbindingResponse()
    }

    override fun getMimeType() = MimeType.JSON

    private fun paymentAuthTokenPresent() = paymentAuthToken != null
}