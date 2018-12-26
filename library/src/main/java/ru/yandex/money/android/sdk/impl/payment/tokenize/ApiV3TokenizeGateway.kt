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

package ru.yandex.money.android.sdk.impl.payment.tokenize

import okhttp3.OkHttpClient
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.PaymentOptionInfo
import ru.yandex.money.android.sdk.impl.ApiMethodException
import ru.yandex.money.android.sdk.impl.ProfilingTool
import ru.yandex.money.android.sdk.impl.ThreatMetrixProfilingTool
import ru.yandex.money.android.sdk.impl.extensions.execute
import ru.yandex.money.android.sdk.methods.TokenRequest
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeGateway
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTokenGateway
import java.util.concurrent.Semaphore

internal class ApiV3TokenizeGateway(
        private val httpClient: Lazy<OkHttpClient>,
        private val shopToken: String,
        private val paymentAuthTokenGateway: PaymentAuthTokenGateway,
        private val tmxProfilingTool: ThreatMetrixProfilingTool
) : TokenizeGateway, ProfilingTool.SessionIdListener {

    private var tmxSessionId: String? = null
    private val semaphore = Semaphore(0)

    override fun onProfilingSessionId(sessionId: String) {
        tmxSessionId = sessionId
        semaphore.release()
    }

    override fun onProfilingError() {
        semaphore.release()
    }

    override fun getToken(
            paymentOption: PaymentOption,
            paymentOptionInfo: PaymentOptionInfo,
            allowRecurringPayments: Boolean
    ): String {
        tmxProfilingTool.requestSessionId(this)
        semaphore.acquire()
        val currentTmxSessionId = tmxSessionId ?: throw TmxProfilingFailedException()
        val paymentAuthToken = paymentAuthTokenGateway.paymentAuthToken
        val tokenRequest = TokenRequest(
                paymentOptionInfo,
                paymentOption,
                currentTmxSessionId,
                shopToken,
                paymentAuthToken)
        tmxSessionId = null
        val response = httpClient.value.execute(tokenRequest)
        when (response.error) {
            null -> return checkNotNull(response.paymentToken)
            else -> throw ApiMethodException(response.error)
        }
    }
}

internal class TmxProfilingFailedException : Exception()
