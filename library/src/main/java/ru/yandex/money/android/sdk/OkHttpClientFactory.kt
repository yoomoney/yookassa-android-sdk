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

package ru.yandex.money.android.sdk

import android.content.Context
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import ru.yandex.money.android.sdk.impl.addUserAgent
import ru.yandex.money.android.sdk.impl.applyLogging
import ru.yandex.money.android.sdk.impl.applySsl
import java.util.concurrent.TimeUnit

private const val DEFAULT_TIMEOUT: Long = 30

internal fun newHttpClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(4, 10L, TimeUnit.MINUTES))
    .followSslRedirects(false)
    .followRedirects(false)
    .applySsl(context)
    .addUserAgent(context)
    .applyLogging()
    .build()
