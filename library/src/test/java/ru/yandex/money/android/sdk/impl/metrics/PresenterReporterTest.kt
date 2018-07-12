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

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.on

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class PresenterReporterTest {

    private val testName = "testName"
    private val testViewModel = "viewmodel"
    private val outputModel = "outputModel"

    @Suppress("UNCHECKED_CAST")
    private val presenter = mock(Presenter::class.java) as Presenter<String, String>
    private val reporter = mock(Reporter::class.java)

    private val allowReporting = arrayOfNulls<Boolean>(1)

    private val presenterReporter = object : PresenterReporter<String, String>(presenter, reporter) {
        override val name = testName
        override fun getArgs(outputModel: String, viewModel: String) = listOf(mock(Param::class.java))
        override fun reportingAllowed(outputModel: String, viewModel: String) = checkNotNull(allowReporting[0])
    }

    @Before
    fun setUp() {
        on(presenter.invoke(outputModel)).thenReturn(testViewModel)
        on(reporter.report(any() ?: testName, any())).then { }
    }

    @Test
    fun shouldReportWhenReportingAllowed() {
        // prepare
        allowReporting[0] = true

        // invoke
        presenterReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report(any() ?: testName, anyList())
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldReportWhenReportingNotAllowed() {
        // prepare
        allowReporting[0] = false

        // invoke
        presenterReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verifyNoMoreInteractions()
        }
    }
}
