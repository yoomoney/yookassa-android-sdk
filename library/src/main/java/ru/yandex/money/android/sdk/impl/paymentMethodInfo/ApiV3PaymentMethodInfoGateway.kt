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

package ru.yandex.money.android.sdk.impl.paymentMethodInfo

import okhttp3.OkHttpClient
import ru.yandex.money.android.sdk.impl.ApiMethodException
import ru.yandex.money.android.sdk.impl.TokensStorage
import ru.yandex.money.android.sdk.impl.extensions.execute
import ru.yandex.money.android.sdk.methods.PaymentMethodRequest
import ru.yandex.money.android.sdk.model.PaymentMethodBankCard
import ru.yandex.money.android.sdk.payment.loadPaymentInfo.PaymentMethodInfoGateway

internal class ApiV3PaymentMethodInfoGateway(
    private val httpClient: Lazy<OkHttpClient>,
    private val tokensStorage: TokensStorage,
    private val shopToken: String
) : PaymentMethodInfoGateway {

    override fun getPaymentMethodInfo(paymentMethodId: String): PaymentMethodBankCard {
        val paymentRequest =
            PaymentMethodRequest(paymentMethodId, shopToken, tokensStorage.userAuthToken)

        val response = httpClient.value.execute(paymentRequest)

        when (response.error) {
            null -> return checkNotNull(response.paymentMethodBankCard)
            else -> throw ApiMethodException(response.error)
        }
    }
}