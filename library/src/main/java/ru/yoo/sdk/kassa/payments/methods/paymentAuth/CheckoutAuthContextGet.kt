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

package ru.yoo.sdk.kassa.payments.methods.paymentAuth

import org.json.JSONObject
import ru.yoo.sdk.kassa.payments.extensions.toAuthContextGetResponse
import ru.yoo.sdk.kassa.payments.model.AuthType
import ru.yoo.sdk.kassa.payments.model.AuthTypeState
import ru.yoo.sdk.kassa.payments.model.Result

private const val AUTH_CONTEXT_GET_PATH = "/checkout/auth-context-get"

private const val AUTH_CONTEXT_ID = "authContextId"

internal class CheckoutAuthContextGetRequest(
        private val authContextId: String,
        userAuthToken: String,
        shopToken: String
) : CheckoutRequest<Result<CheckoutAuthContextGetResponse>>(userAuthToken, shopToken) {

    override fun getUrl(): String = host + AUTH_CONTEXT_GET_PATH

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toAuthContextGetResponse()

    override fun getPayload() = listOf(AUTH_CONTEXT_ID to authContextId)
}

internal data class CheckoutAuthContextGetResponse(
    val authTypeStates: Array<AuthTypeState>,
    val defaultAuthType: AuthType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckoutAuthContextGetResponse

        if (!authTypeStates.contentEquals(other.authTypeStates)) return false
        if (defaultAuthType != other.defaultAuthType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authTypeStates.contentHashCode()
        result = 31 * result + defaultAuthType.hashCode()
        return result
    }
}
