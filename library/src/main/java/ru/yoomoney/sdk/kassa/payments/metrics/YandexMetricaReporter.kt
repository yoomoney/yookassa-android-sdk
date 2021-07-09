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

package ru.yoomoney.sdk.kassa.payments.metrics

import android.util.Log
import com.yandex.metrica.IReporter
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.NoInternetException
import ru.yoomoney.sdk.kassa.payments.model.PassphraseCheckFailedException
import ru.yoomoney.sdk.kassa.payments.model.RequestExecutionException
import ru.yoomoney.sdk.kassa.payments.model.ResponseParsingException
import ru.yoomoney.sdk.kassa.payments.model.ResponseReadingException
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoomoney.sdk.kassa.payments.model.UnhandledException

internal class YandexMetricaReporter(
    private val metrica: IReporter
) : Reporter {

    override fun report(name: String, args: List<Param>?) =
        metrica.reportEvent(name,
            (args?.associate { it.name to it.value } ?: emptyMap()) + mapOf("msdkVersion" to BuildConfig.VERSION_NAME)
        )

    override fun report(name: String, arg: String) {
        metrica.reportEvent(name, mapOf(arg to "") + mapOf("msdkVersion" to BuildConfig.VERSION_NAME))
    }
}

internal class YandexMetricaErrorReporter(
    private val metrica: IReporter
) : ErrorReporter {

    override fun report(e: SdkException) {
        when(e) {
            is SelectedOptionNotFoundException -> metrica.reportError("Selected option not found error", e)
            is RequestExecutionException -> {
                metrica.reportError("Request execution error", e.e)
            }
            is NoInternetException ->  metrica.reportError("No internet error", e)
            is ResponseReadingException -> metrica.reportError("Response reading error", e.e)
            is ResponseParsingException -> metrica.reportError("No internet error", e.e)
            is ApiMethodException -> metrica.reportError("Api method error", e)
            is AuthCheckApiMethodException -> metrica.reportError("Auth check api method error", e)
            is PassphraseCheckFailedException -> metrica.reportError("Passphrase check failed error", e)
            else ->  metrica.reportError("Unknown sdk error", e)
        }
    }
}

internal class YandexMetricaExceptionReporter(
    private val metrica: IReporter
) : ExceptionReporter {

    override fun report(e: UnhandledException) = metrica.reportUnhandledException(e)
}

internal class YandexMetricaSessionReporter(
    private val metrica: IReporter
) : SessionReporter {

    override fun resumeSession() = metrica.resumeSession()

    override fun pauseSession() = metrica.pauseSession()
}

internal class YandexMetricaLoggerReporter(
    private val showLogs: Boolean,
    private val errorReporter: ErrorReporter
) : ErrorLoggerReporter {

    companion object {
        private const val ERROR_TAG = "ERROR"
    }

    override fun report(e: SdkException) {
        errorReporter.report(e)
        if (showLogs) {
            Log.d(ERROR_TAG, e.toString())
        }
    }
}
