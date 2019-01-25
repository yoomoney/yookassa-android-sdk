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

import org.json.JSONObject
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.impl.extensions.toWalletCheckResponse
import ru.yandex.money.android.sdk.methods.base.MimeType
import ru.yandex.money.android.sdk.methods.base.PostRequest
import ru.yandex.money.android.sdk.model.Status

private const val WALLET_CHECK = "/wallet-check"

internal data class WalletCheckRequest(
        private val userAuthToken: String
) : PostRequest<WalletCheckResponse> {

    override fun getMimeType() = MimeType.X_WWW_FORM_URLENCODED

    override fun getPayload() = listOf("oauth_token" to userAuthToken)

    override fun getUrl() = BuildConfig.API_V1_HOST + WALLET_CHECK

    override fun getHeaders() = listOf("X-Forwarded-For" to "127.0.0.1")

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toWalletCheckResponse()
}

internal data class WalletCheckResponse(
    val status: Status,
    val hasWallet: Boolean?
)
