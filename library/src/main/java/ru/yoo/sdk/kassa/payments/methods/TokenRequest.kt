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

package ru.yoo.sdk.kassa.payments.methods

import okhttp3.Credentials
import org.json.JSONObject
import ru.yoo.sdk.kassa.payments.BuildConfig
import ru.yoo.sdk.kassa.payments.extensions.toJsonObject
import ru.yoo.sdk.kassa.payments.extensions.toTokenResponse
import ru.yoo.sdk.kassa.payments.methods.base.MimeType
import ru.yoo.sdk.kassa.payments.methods.base.PostRequest
import ru.yoo.sdk.kassa.payments.model.Confirmation
import ru.yoo.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.Result

private const val TOKEN_METHOD_PATH = "/tokens"
private const val PAYMENT_METHOD_DATA = "payment_method_data"
private const val TMX_SESSION_ID = "tmx_session_id"
private const val AMOUNT = "amount"
private const val CONFIRMATION = "confirmation"
private const val PAYMENT_METHOD_ID = "payment_method_id"
private const val SAVE_PAYMENT_METHOD = "save_payment_method"
private const val CSC = "csc"

internal data class TokenRequest(
    private val paymentOptionInfo: PaymentOptionInfo,
    private val paymentOption: PaymentOption,
    private val tmxSessionId: String,
    private val shopToken: String,
    private val paymentAuthToken: String?,
    private val confirmation: Confirmation,
    private val savePaymentMethod: Boolean
) : PostRequest<Result<String>> {

    override fun getHeaders() = listOf("Authorization" to Credentials.basic(shopToken, "")).let { headers ->
        headers.takeIf { paymentAuthTokenPresent() }?.plus("Wallet-Authorization" to "Bearer $paymentAuthToken")
            ?: headers
    }

    override fun getPayload(): List<Pair<String, Any>> {
        return listOf(
            TMX_SESSION_ID to tmxSessionId,
            AMOUNT to paymentOption.charge.toJsonObject(),
            SAVE_PAYMENT_METHOD to savePaymentMethod
        ).let { payload ->
            val params = if (paymentOption is PaymentIdCscConfirmation) {
                payload + (PAYMENT_METHOD_ID to paymentOption.paymentMethodId) + (CSC to (paymentOptionInfo as LinkedCardInfo).csc)
            } else {
                payload + (PAYMENT_METHOD_DATA to paymentOptionInfo.toJsonObject(paymentOption))
            }
            confirmation.toJsonObject()?.let { params + (CONFIRMATION to it) } ?: params
        }
    }

    override fun getMimeType() = MimeType.JSON

    override fun getUrl(): String = BuildConfig.HOST + TOKEN_METHOD_PATH

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toTokenResponse()

    private fun paymentAuthTokenPresent() = paymentAuthToken != null
}
