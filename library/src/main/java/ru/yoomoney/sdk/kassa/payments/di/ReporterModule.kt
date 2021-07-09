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

import com.yandex.metrica.IReporter
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.logging.ReporterLogger
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporterImpl
import ru.yoomoney.sdk.kassa.payments.metrics.ExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SessionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaErrorReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaLoggerReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaSessionReporter
import javax.inject.Singleton

@Module
internal class ReporterModule {

    @Provides
    @Singleton
    fun reporter(metrica: IReporter): Reporter {
        return ReporterLogger(YandexMetricaReporter(metrica))
    }

    @Provides
    @Singleton
    fun errorReporter(metrica: IReporter): ErrorReporter {
        return YandexMetricaErrorReporter(metrica)
    }

    @Provides
    @Singleton
    fun errorLoggerReporter(
        testParameters: TestParameters,
        metrica: IReporter
    ): ErrorLoggerReporter {
        return YandexMetricaLoggerReporter(testParameters.showLogs, YandexMetricaErrorReporter(metrica))
    }

    @Provides
    @Singleton
    fun exceptionReporter(metrica: IReporter): ExceptionReporter {
        return YandexMetricaExceptionReporter(metrica)
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
}
