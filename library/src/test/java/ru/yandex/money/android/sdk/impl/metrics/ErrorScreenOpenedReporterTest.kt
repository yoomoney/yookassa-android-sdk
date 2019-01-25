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
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.model.Presenter
import ru.yandex.money.android.sdk.model.SdkException
import ru.yandex.money.android.sdk.on

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ErrorScreenOpenedReporterTest {

    private val testAuthType = AuthTypeWithoutAuth()
    private val testTokenizeScheme = TokenizeSchemeWallet()

    @Mock
    private lateinit var authTypeProvider: AuthTypeProvider
    @Mock
    private lateinit var tokenizeSchemeProvider: TokenizeSchemeParamProvider
    @Mock
    private lateinit var presenter: Presenter<Exception, Any>
    @Mock
    private lateinit var reporter: Reporter

    private lateinit var errorScreenOpenedReporter: ErrorScreenOpenedReporter<Any>

    @Before
    fun setUp() {
        on(authTypeProvider()).thenReturn(testAuthType)
        on(presenter.invoke(any() ?: Exception())).thenReturn(Unit)

        errorScreenOpenedReporter = ErrorScreenOpenedReporter(
            getAuthType = authTypeProvider,
            getTokenizeScheme = tokenizeSchemeProvider,
            presenter = presenter,
            reporter = reporter
        )
    }

    @Test
    fun `should not report for not SdkException`() {
        // prepare
        val exception = Exception()

        // invoke
        errorScreenOpenedReporter(exception)

        // assert
        inOrder(authTypeProvider, tokenizeSchemeProvider, presenter, reporter).apply {
            verify(presenter).invoke(exception)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report AythType only when TokenizeScheme not present`() {
        // prepare
        on(tokenizeSchemeProvider()).thenReturn(null)
        val exception = SdkException()

        // invoke
        errorScreenOpenedReporter(exception)

        // assert
        inOrder(authTypeProvider, tokenizeSchemeProvider, presenter, reporter).apply {
            verify(presenter).invoke(exception)
            verify(tokenizeSchemeProvider).invoke()
            verify(authTypeProvider).invoke()
            verify(reporter).report("screenError", listOf(testAuthType))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report AuthType and TokenizeScheme if TokenizeScheme present`() {
        // prepare
        on(tokenizeSchemeProvider()).thenReturn(testTokenizeScheme)
        val exception = SdkException()

        // invoke
        errorScreenOpenedReporter(exception)

        // assert
        inOrder(authTypeProvider, tokenizeSchemeProvider, presenter, reporter).apply {
            verify(presenter).invoke(exception)
            verify(tokenizeSchemeProvider).invoke()
            verify(authTypeProvider).invoke()
            verify(reporter).report("screenError", listOf(testAuthType, testTokenizeScheme))
            verifyNoMoreInteractions()
        }
    }

    private interface AuthTypeProvider : () -> AuthType
    private interface TokenizeSchemeParamProvider : () -> TokenizeScheme?
}
