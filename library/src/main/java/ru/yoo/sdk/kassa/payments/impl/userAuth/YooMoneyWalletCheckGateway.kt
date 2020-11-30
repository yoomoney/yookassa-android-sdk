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

package ru.yoo.sdk.kassa.payments.impl.userAuth

import okhttp3.OkHttpClient
import ru.yoo.sdk.kassa.payments.impl.ApiMethodException
import ru.yoo.sdk.kassa.payments.impl.extensions.execute
import ru.yoo.sdk.kassa.payments.methods.WalletCheckRequest
import ru.yoo.sdk.kassa.payments.model.ErrorCode
import ru.yoo.sdk.kassa.payments.model.Status
import ru.yoo.sdk.kassa.payments.userAuth.WalletCheckGateway

internal class YooMoneyWalletCheckGateway(
        private val httpClient: Lazy<OkHttpClient>
) : WalletCheckGateway {

    override fun checkIfUserHasWallet(userAuthToken: String): Boolean {
        val request = WalletCheckRequest(userAuthToken)
        val response = httpClient.value.execute(request)

        if (response.status == Status.SUCCESS) {
            return response.hasWallet ?: throw ApiMethodException(ErrorCode.TECHNICAL_ERROR)
        } else {
            throw ApiMethodException(ErrorCode.TECHNICAL_ERROR)
        }
    }
}
