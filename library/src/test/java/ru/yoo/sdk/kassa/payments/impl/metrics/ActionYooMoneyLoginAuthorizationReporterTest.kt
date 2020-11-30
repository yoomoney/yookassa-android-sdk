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

package ru.yoo.sdk.kassa.payments.impl.metrics

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthCancelledViewModel
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthSuccessViewModel
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthViewModel
import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthCancelledOutputModel
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthOutputModel
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthSuccessOutputModel

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionYooMoneyLoginAuthorizationReporterTest {

    private val name = "actionYaLoginAuthorization"

    @Mock
    private lateinit var presenter: Presenter<UserAuthOutputModel, UserAuthViewModel>
    @Mock
    private lateinit var reporter: Reporter
    private lateinit var actionReporter: ActionYooMoneyLoginAuthorizationReporter

    @Before
    fun setUp() {
        actionReporter = ActionYooMoneyLoginAuthorizationReporter(presenter, reporter)
    }

    @Test
    fun `should report AuthYooMoneyLoginStatusSuccess when UserAuthSuccessOutputModel`() {
        // prepare
        val outputModel =
            UserAuthSuccessOutputModel(AuthorizedUser())
        on(presenter(outputModel)).thenReturn(UserAuthSuccessViewModel(outputModel.authorizedUser))

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report(name, listOf(AuthYooMoneyLoginStatusSuccess()))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report AuthYaLoginStatusCanceled when UserAuthCancelledOutputModel`() {
        // prepare
        val outputModel = UserAuthCancelledOutputModel()
        on(presenter(outputModel)).thenReturn(UserAuthCancelledViewModel)

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report(name, listOf(AuthYooMoneyLoginStatusCanceled()))
            verifyNoMoreInteractions()
        }
    }
}
