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
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthFailViewModel
import ru.yandex.money.android.sdk.on

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionYaLoginAuthorizationFailedReporterTest {

    @Mock
    private lateinit var presenter: Presenter<Exception, UserAuthFailViewModel>
    @Mock
    private lateinit var reporter: Reporter
    private lateinit var actionReporter: ActionYaLoginAuthorizationFailedReporter

    @Before
    fun setUp() {
        actionReporter = ActionYaLoginAuthorizationFailedReporter(presenter, reporter)
    }

    @Test
    fun `should report AuthYaLoginStatusFail when Exception`() {
        // prepare
        val exception = Exception()
        on(presenter(exception)).thenReturn(UserAuthFailViewModel("err"))

        // invoke
        actionReporter(exception)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(exception)
            verify(reporter).report("actionYaLoginAuthorization", listOf(AuthYaLoginStatusFail()))
            verifyNoMoreInteractions()
        }
    }
}
