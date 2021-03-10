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

package ru.yoo.sdk.kassa.payments.userAuth

import ru.yoo.sdk.kassa.payments.metrics.AuthYooMoneyLoginStatusCanceled
import ru.yoo.sdk.kassa.payments.metrics.AuthYooMoneyLoginStatusSuccess
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.march.Logic
import ru.yoo.sdk.march.Out

internal class MoneyAuthAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<MoneyAuth.State, MoneyAuth.Action>
) : Logic<MoneyAuth.State, MoneyAuth.Action> {

    private val name = "actionLoginAuthorization"

    override fun invoke(
        state: MoneyAuth.State,
        action: MoneyAuth.Action
    ): Out<MoneyAuth.State, MoneyAuth.Action> {
        val arg = when (action) {
            is MoneyAuth.Action.Authorized -> AuthYooMoneyLoginStatusSuccess()
            MoneyAuth.Action.AuthCancelled -> AuthYooMoneyLoginStatusCanceled()
            else -> null
        }

        arg?.let { reporter.report(name, listOf(arg)) }

        return businessLogic(state, action)
    }

}