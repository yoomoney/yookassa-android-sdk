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
import com.yandex.metrica.IReporter
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.UserProfile
import dagger.Module
import dagger.Provides
import ru.yoo.sdk.kassa.payments.BuildConfig
import ru.yoo.sdk.kassa.payments.logging.ReporterLogger
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoo.sdk.kassa.payments.metrics.ErrorScreenReporterImpl
import ru.yoo.sdk.kassa.payments.metrics.ExceptionReporter
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.kassa.payments.metrics.SessionReporter
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoo.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoo.sdk.kassa.payments.metrics.YandexMetricaErrorReporter
import ru.yoo.sdk.kassa.payments.metrics.YandexMetricaExceptionReporter
import ru.yoo.sdk.kassa.payments.metrics.YandexMetricaReporter
import ru.yoo.sdk.kassa.payments.metrics.YandexMetricaSessionReporter
import javax.inject.Singleton

@Module
internal class ReporterModule {

    @Provides
    @Singleton
    fun yandexMetricaReporter(context: Context): IReporter {
        return YandexMetrica.getReporter(context.applicationContext, BuildConfig.APP_METRICA_KEY)
    }

    @Provides
    @Singleton
    fun errorReporter(metrica: IReporter): ErrorReporter {
        return YandexMetricaErrorReporter(metrica)
    }

    @Provides
    @Singleton
    fun reporter(metrica: IReporter): Reporter {
        return ReporterLogger(YandexMetricaReporter(metrica))
    }

    @Provides
    @Singleton
    fun sessionReporter(metrica: IReporter): SessionReporter {
        return YandexMetricaSessionReporter(metrica)
    }

    @Provides
    @Singleton
    fun errorScreenReporter(
        reporter: Reporter,
        userAuthTypeParamProvider: UserAuthTypeParamProvider,
        tokenizeSchemeParamProvider: TokenizeSchemeParamProvider
    ): ErrorScreenReporter {
        return ErrorScreenReporterImpl(
            reporter = reporter,
            getAuthType = userAuthTypeParamProvider,
            getTokenizeScheme = tokenizeSchemeParamProvider
        )
    }

    @Provides
    @Singleton
    fun exceptionReporter(metrica: IReporter): ExceptionReporter {
        return YandexMetricaExceptionReporter(metrica)
    }
}
