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

package ru.yoomoney.sdk.kassa.payments.userAuth

import ru.yoomoney.sdk.auth.account.model.UserAccount
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginScheme

internal object MoneyAuth {

    sealed class State {
        object WaitingForAuthStarted : State()
        data class Authorize(val authorizeStrategy: AuthorizeStrategy) : State()
        object CancelAuth : State()
        object CompleteAuth : State()
    }

    sealed class Action {
        data class RequireAuth(val isYooMoneyCouldBeOpened: Boolean) : Action()

        data class GetTransferData(val cryptogram: String) : Action()

        data class Authorized(
            val token: String?,
            val userAccount: UserAccount?,
            val tmxSessionId: String?,
            val typeAuth: MoneyAuthLoginScheme
        ) : Action()

        object AuthSuccessful : Action()
        object AuthCancelled : Action()
        object AuthFailed : Action()
    }

    sealed class AuthorizeStrategy {
        object InApp : AuthorizeStrategy()
        object App2App : AuthorizeStrategy()
    }
}