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
import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodRepository
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodRepositoryImpl
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingTool
import ru.yoomoney.sdk.kassa.payments.tmx.ThreatMetrixProfilingTool
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.navigation.AppRouter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.model.Executor
import ru.yoomoney.sdk.kassa.payments.errorFormatter.DefaultErrorFormatter
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.getConfirmation
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.utils.DEFAULT_REDIRECT_URL
import ru.yoomoney.sdk.kassa.payments.utils.getSberbankPackage
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
    fun getConfirmation(context: Context, paymentParameters: PaymentParameters, testParameters: TestParameters): GetConfirmation {
        val sberbankPackage = getSberbankPackage(testParameters.hostParameters.isDevHost)
        return object : GetConfirmation {
            override fun invoke(p1: PaymentOption): Confirmation {
                return p1.getConfirmation(context,
                    paymentParameters.customReturnUrl ?: DEFAULT_REDIRECT_URL,
                    context.resources.getString(R.string.ym_app_scheme),
                    sberbankPackage
                )
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
    fun providePaymentOptionRepository(): PaymentMethodRepository {
        return PaymentMethodRepositoryImpl(null, null)
    }
}