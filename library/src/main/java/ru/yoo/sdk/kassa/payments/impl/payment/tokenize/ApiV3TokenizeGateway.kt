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

package ru.yoo.sdk.kassa.payments.impl.payment.tokenize

import okhttp3.OkHttpClient
import ru.yoo.sdk.kassa.payments.impl.ApiMethodException
import ru.yoo.sdk.kassa.payments.impl.ProfilingTool
import ru.yoo.sdk.kassa.payments.impl.ThreatMetrixProfilingTool
import ru.yoo.sdk.kassa.payments.impl.TmxSessionIdStorage
import ru.yoo.sdk.kassa.payments.impl.extensions.execute
import ru.yoo.sdk.kassa.payments.methods.TokenRequest
import ru.yoo.sdk.kassa.payments.model.Confirmation
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeGateway
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTokenGateway
import java.util.concurrent.Semaphore

internal class ApiV3TokenizeGateway(
    private val httpClient: Lazy<OkHttpClient>,
    private val shopToken: String,
    private val paymentAuthTokenGateway: PaymentAuthTokenGateway,
    private val tmxSessionIdStorage: TmxSessionIdStorage,
    private val tmxProfilingTool: ThreatMetrixProfilingTool
) : TokenizeGateway, ProfilingTool.SessionIdListener {

    private var tmxSessionId: String? = null
    private val semaphore = Semaphore(0)

    override fun onProfilingSessionId(sessionId: String) {
        tmxSessionId = sessionId
        semaphore.release()
    }

    override fun onProfilingError(status: String) {
        tmxSessionId = status
        semaphore.release()
    }

    override fun getToken(
        paymentOption: PaymentOption,
        paymentOptionInfo: PaymentOptionInfo,
        savePaymentMethod: Boolean,
        confirmation: Confirmation
    ): String {
        tmxSessionId = tmxSessionIdStorage.tmxSessionId
        if (tmxSessionId.isNullOrEmpty()) {
            tmxProfilingTool.requestSessionId(this)
            semaphore.acquire()
        }
        val currentTmxSessionId = tmxSessionId ?: throw TmxProfilingFailedException()
        val paymentAuthToken = paymentAuthTokenGateway.paymentAuthToken
        val tokenRequest = TokenRequest(
            paymentOptionInfo,
            paymentOption,
            currentTmxSessionId,
            shopToken,
            paymentAuthToken,
            confirmation,
            savePaymentMethod
        )
        tmxSessionId = null
        tmxSessionIdStorage.tmxSessionId = null
        val response = httpClient.value.execute(tokenRequest)
        when (response.error) {
            null -> return checkNotNull(response.paymentToken)
            else -> throw ApiMethodException(response.error)
        }
    }
}

internal class TmxProfilingFailedException : Exception()
