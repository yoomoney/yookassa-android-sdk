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

package ru.yoomoney.sdk.kassa.payments.methods.paymentAuth

import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.extensions.toAuthCheckResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toJsonString
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.Result

private const val AUTH_CHECK_PATH = "/checkout/auth-check"

private const val AUTH_CONTEXT_ID = "authContextId"
private const val AUTH_TYPE = "authType"
private const val ANSWER = "answer"

internal class CheckoutAuthCheckRequest(
    private val authContextId: String,
    private val authType: AuthType,
    private val answer: String,
    userAuthToken: String,
    shopToken: String,
    hostProvider: HostProvider
) : CheckoutRequest<Result<Unit>>(userAuthToken, shopToken, hostProvider) {

    override fun getUrl(): String = host + AUTH_CHECK_PATH

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toAuthCheckResponse()

    override fun getPayload() = listOf(
            AUTH_CONTEXT_ID to authContextId,
            AUTH_TYPE to authType.toJsonString(),
            ANSWER to answer)
}
