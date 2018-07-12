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

import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthFailViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthViewModel
import ru.yandex.money.android.sdk.userAuth.UserAuthCancelledOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthNoWalletOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthSuccessOutputModel

private const val NAME = "actionYaLoginAuthorization"

internal class ActionYaLoginAuthorizationReporter(
    presenter: Presenter<UserAuthOutputModel, UserAuthViewModel>,
    reporter: Reporter
) : PresenterReporter<UserAuthOutputModel, UserAuthViewModel>(presenter, reporter) {

    override val name = NAME

    override fun getArgs(outputModel: UserAuthOutputModel, viewModel: UserAuthViewModel) = listOf(
        when (outputModel) {
            is UserAuthSuccessOutputModel -> AuthYaLoginStatusSuccess()
            is UserAuthCancelledOutputModel -> AuthYaLoginStatusCanceled()
            is UserAuthNoWalletOutputModel -> AuthYaLoginStatusWithoutWallet()
        }
    )

    override fun reportingAllowed(outputModel: UserAuthOutputModel, viewModel: UserAuthViewModel) = true
}

internal class ActionYaLoginAuthorizationFailedReporter(
    presenter: Presenter<Exception, UserAuthFailViewModel>,
    reporter: Reporter
) : PresenterReporter<Exception, UserAuthFailViewModel>(presenter, reporter) {

    override val name = NAME

    override fun getArgs(outputModel: Exception, viewModel: UserAuthFailViewModel) = listOf(AuthYaLoginStatusFail())

    override fun reportingAllowed(outputModel: Exception, viewModel: UserAuthFailViewModel) = true

}
