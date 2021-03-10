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

package ru.yoo.sdk.kassa.payments.di

import android.content.Context
import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import ru.yoo.sdk.kassa.payments.payment.PaymentOptionRepository
import ru.yoo.sdk.kassa.payments.payment.PaymentOptionRepositoryImpl
import ru.yoo.sdk.kassa.payments.tmx.ProfilingTool
import ru.yoo.sdk.kassa.payments.tmx.ThreatMetrixProfilingTool
import ru.yoo.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoo.sdk.kassa.payments.navigation.AppRouter
import ru.yoo.sdk.kassa.payments.navigation.Router
import ru.yoo.sdk.kassa.payments.model.Executor
import ru.yoo.sdk.kassa.payments.errorFormatter.DefaultErrorFormatter
import ru.yoo.sdk.kassa.payments.errorFormatter.ErrorFormatter
import javax.inject.Singleton

@Module
internal class CoreModule {

    @Provides
    @Singleton
    fun mainExecutor(): Executor {
        val mainHandler = Handler(Looper.getMainLooper())
        return object: Executor {
            override fun invoke(p1: () -> Unit) {
                mainHandler.post(p1)
            }
        }
    }

    @Provides
    @Singleton
    fun defaultErrorFormatter(context: Context): ErrorFormatter {
        return DefaultErrorFormatter(context)
    }

    @Provides
    @Singleton
    fun tmxSessionIdStorage(): TmxSessionIdStorage {
        return TmxSessionIdStorage()
    }

    @Provides
    @Singleton
    fun profilingTool(context: Context): ProfilingTool {
        return ThreatMetrixProfilingTool().apply {
            init(context.applicationContext)
        }
    }

    @Provides
    @Singleton
    fun provideRouter(): Router {
        return AppRouter()
    }

    @Provides
    @Singleton
    fun providePaymentOptionRepository(): PaymentOptionRepository {
        return PaymentOptionRepositoryImpl(null)
    }
}