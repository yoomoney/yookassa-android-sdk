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

package ru.yandex.money.android.sdk.impl.paymentOptionList

import android.content.Context
import okhttp3.OkHttpClient
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.impl.ApiMethodException
import ru.yandex.money.android.sdk.impl.TokensStorage
import ru.yandex.money.android.sdk.impl.extensions.execute
import ru.yandex.money.android.sdk.methods.PaymentOptionsRequest
import ru.yandex.money.android.sdk.model.CurrentUser
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListGateway

internal class ApiV3PaymentOptionListGateway(
    private val httpClient: Lazy<OkHttpClient>,
    private val gatewayId: String?,
    private val tokensStorage: TokensStorage,
    private val shopToken: String
) : PaymentOptionListGateway {

    override fun getPaymentOptions(amount: Amount, currentUser: CurrentUser): List<PaymentOption> {
        val paymentRequest =
            PaymentOptionsRequest(amount, currentUser, gatewayId, tokensStorage.userAuthToken, shopToken)

        val response = httpClient.value.execute(paymentRequest)

        response.error?.also { throw ApiMethodException(it) }

        return response.paymentOptions
    }
}
