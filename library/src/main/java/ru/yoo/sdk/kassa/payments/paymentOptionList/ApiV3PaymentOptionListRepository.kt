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

package ru.yoo.sdk.kassa.payments.paymentOptionList

import okhttp3.OkHttpClient
import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoo.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoo.sdk.kassa.payments.secure.TokensStorage
import ru.yoo.sdk.kassa.payments.extensions.execute
import ru.yoo.sdk.kassa.payments.methods.PaymentOptionsRequest
import ru.yoo.sdk.kassa.payments.model.CurrentUser
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.model.Result

internal class ApiV3PaymentOptionListRepository(
    private val httpClient: Lazy<CheckoutOkHttpClient>,
    private val gatewayId: String?,
    private val tokensStorage: TokensStorage,
    private val shopToken: String,
    private val savePaymentMethod: SavePaymentMethod
) : PaymentOptionListRepository {

    override fun getPaymentOptions(amount: Amount, currentUser: CurrentUser): Result<List<PaymentOption>> {
        val userAuthToken: String? = if (!tokensStorage.passportAuthToken.isNullOrEmpty() && !tokensStorage.paymentAuthToken.isNullOrEmpty()) {
            tokensStorage.passportAuthToken
        } else {
            tokensStorage.userAuthToken
        }
        val paymentRequest = PaymentOptionsRequest(
            amount = amount,
            currentUser = currentUser,
            gatewayId = gatewayId,
            userAuthToken = userAuthToken,
            shopToken = shopToken,
            savePaymentMethod = savePaymentMethod
        )
        return httpClient.value.execute(paymentRequest)
    }
}
