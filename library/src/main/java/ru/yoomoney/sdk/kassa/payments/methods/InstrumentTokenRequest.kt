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
import ru.yoomoney.sdk.kassa.payments.extensions.toJsonObject
import ru.yoomoney.sdk.kassa.payments.extensions.toTokenResponse
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.methods.base.MimeType
import ru.yoomoney.sdk.kassa.payments.methods.base.PostRequest
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.Result

private const val TOKEN_METHOD_PATH = "/tokens"
private const val TMX_SESSION_ID = "tmx_session_id"
private const val AMOUNT = "amount"
private const val SAVE_PAYMENT_METHOD = "save_payment_method"
private const val CSC = "csc"
private const val CONFIRMATION = "confirmation_type"
private const val PAYMENT_INSTRUMENT_ID = "payment_instrument_id"

internal data class InstrumentTokenRequest(
    private val hostProvider: HostProvider,
    private val amount: Amount,
    private val tmxSessionId: String,
    private val shopToken: String,
    private val paymentAuthToken: String?,
    private val confirmation: Confirmation,
    private val savePaymentMethod: Boolean,
    private val csc: String?,
    private val instrumentBankCard: PaymentInstrumentBankCard
) : PostRequest<Result<String>> {

    override fun getHeaders() = listOf("Authorization" to Credentials.basic(shopToken, "")).let { headers ->
        headers.takeIf { paymentAuthTokenPresent() }?.plus("Wallet-Authorization" to "Bearer $paymentAuthToken")
            ?: headers
    }

    override fun getPayload(): List<Pair<String, Any>> {
        return listOf(
            TMX_SESSION_ID to tmxSessionId,
            AMOUNT to amount.toJsonObject(),
            SAVE_PAYMENT_METHOD to savePaymentMethod,
            PAYMENT_INSTRUMENT_ID to instrumentBankCard.paymentInstrumentId
        ).let { payload ->
            csc?.let { payload.plus(CSC to csc) } ?: payload
        }.let { payload ->
            confirmation.toJsonObject()?.let { payload + (CONFIRMATION to it) } ?: payload
        }
    }

    override fun getMimeType() = MimeType.JSON

    override fun getUrl(): String = hostProvider.host() + TOKEN_METHOD_PATH

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toTokenResponse()

    private fun paymentAuthTokenPresent() = paymentAuthToken != null
}
