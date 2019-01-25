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
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthCancelledViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthNoWalletViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthSuccessViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthViewModel
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.model.Presenter
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.userAuth.UserAuthCancelledOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthNoWalletOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthSuccessOutputModel

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionYaLoginAuthorizationReporterTest {

    private val name = "actionYaLoginAuthorization"

    @Mock
    private lateinit var presenter: Presenter<UserAuthOutputModel, UserAuthViewModel>
    @Mock
    private lateinit var reporter: Reporter
    private lateinit var actionReporter: ActionYaLoginAuthorizationReporter

    @Before
    fun setUp() {
        actionReporter = ActionYaLoginAuthorizationReporter(presenter, reporter)
    }

    @Test
    fun `should report AuthYaLoginStatusSuccess when UserAuthSuccessOutputModel`() {
        // prepare
        val outputModel = UserAuthSuccessOutputModel(AuthorizedUser("name"))
        on(presenter(outputModel)).thenReturn(UserAuthSuccessViewModel(outputModel.authorizedUser))

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report(name, listOf(AuthYaLoginStatusSuccess()))
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
            verify(reporter).report(name, listOf(AuthYaLoginStatusCanceled()))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report AuthYaLoginStatusWithoutWallet when UserAuthNoWalletOutputModel`() {
        // prepare
        val outputModel = UserAuthNoWalletOutputModel("name")
        on(presenter(outputModel)).thenReturn(UserAuthNoWalletViewModel(outputModel.accountName))

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report(name, listOf(AuthYaLoginStatusWithoutWallet()))
            verifyNoMoreInteractions()
        }
    }
}
