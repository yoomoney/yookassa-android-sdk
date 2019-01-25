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

package ru.yandex.money.android.sdk.impl.metrics

import ru.yandex.money.android.sdk.model.Presenter
import ru.yandex.money.android.sdk.model.SdkException
import ru.yandex.money.android.sdk.model.UnhandledException

internal interface Reporter {
    fun report(name: String, args: List<Param>? = null)
}

internal interface ErrorReporter {
    fun report(e: SdkException)
}

internal interface ExceptionReporter {
    fun report(e: UnhandledException)
}

internal interface SessionReporter {
    fun resumeSession()
    fun pauseSession()
}

internal abstract class PresenterReporter<in I, O>(
    private val presenter: Presenter<I, O>,
    private val reporter: Reporter
) : Presenter<I, O> {

    protected abstract val name: String

    final override fun invoke(outputModel: I) = presenter(outputModel).also { report(outputModel, it) }

    protected abstract fun getArgs(outputModel: I, viewModel: O): List<Param>?

    protected abstract fun reportingAllowed(outputModel: I, viewModel: O): Boolean

    private fun report(outputModel: I, viewModel: O) {
        reporter.takeIf { reportingAllowed(outputModel, viewModel) }?.report(name, getArgs(outputModel, viewModel))
    }
}
