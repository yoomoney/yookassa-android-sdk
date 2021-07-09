/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.http

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.yoomoney.sdk.kassa.payments.utils.isBuildDebug
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val DEFAULT_TIMEOUT: Long = 30

internal fun newHttpClient(
    context: Context,
    showLogs: Boolean,
    isDevHost: Boolean
): OkHttpClient = OkHttpClient.Builder()
    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(4, 10L, TimeUnit.MINUTES))
    .followSslRedirects(false)
    .followRedirects(false)
    .applySsl(isDevHost)
    .addUserAgent(context)
    .applyLogging(context, showLogs)
    .build()

internal fun OkHttpClient.Builder.applyLogging(
    context: Context,
    showLogs: Boolean
) = if (context.isBuildDebug() && showLogs) {
    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
} else {
    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE))
}

internal fun OkHttpClient.Builder.applySsl(isDevHost: Boolean) = if (isDevHost) {
    apply {
        sslSocketFactory(sslSocketFactory(), debugTrustManager[0] as X509TrustManager)
        hostnameVerifier(HostnameVerifier { _, _ -> true })
    }
} else {
    this
}

private fun sslSocketFactory() =
    SSLContext.getInstance("SSL").apply { init(null, debugTrustManager, SecureRandom()) }.socketFactory

private val debugTrustManager = arrayOf<TrustManager>(object : X509TrustManager {

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        // does nothing
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        // does nothing
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {

        return arrayOf()
    }
})