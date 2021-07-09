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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import com.google.android.gms.security.ProviderInstaller
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.model.SdkException

@Module
internal class HttpModule {

    @Provides
    fun httpClient(
        context: Context,
        okHttpClient: OkHttpClient,
        errorReporter: ErrorLoggerReporter
    ): CheckoutOkHttpClient {
        try {
            ProviderInstaller.installIfNeeded(context)
        } catch (e: Exception) {
            errorReporter.report(SdkException(e))
        }
        return CheckoutOkHttpClient(okHttpClient, errorReporter)
    }

    @Provides
    fun hostProvider(testParameters: TestParameters): HostProvider {
        return object : HostProvider {
            override fun host(): String = testParameters.hostParameters.host
            override fun paymentAuthorizationHost(): String = testParameters.hostParameters.paymentAuthorizationHost
            override fun authHost(): String? = testParameters.hostParameters.authHost
        }
    }
}