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

package ru.yoomoney.sdk.kassa.payments.unbind

import ru.yoomoney.sdk.kassa.payments.metrics.ActionUnbindCardStatusFail
import ru.yoomoney.sdk.kassa.payments.metrics.ActionUnbindCardStatusSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.LinkedCardTypeBankCard
import ru.yoomoney.sdk.kassa.payments.metrics.LinkedCardTypeWallet
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out

private const val ACTION_UNBIND_CARD = "actionUnbindBankCard"
private const val SCREEN_BANK_CARD = "screenUnbindCard"

internal class UnbindCardAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<UnbindCard.State, UnbindCard.Action>
) : Logic<UnbindCard.State, UnbindCard.Action> {

    override fun invoke(
        state: UnbindCard.State,
        action: UnbindCard.Action
    ): Out<UnbindCard.State, UnbindCard.Action> {
        val (name, args) = when (action) {
            is UnbindCard.Action.StartDisplayData -> if (action.instrumentBankCard != null) {
                SCREEN_BANK_CARD to listOf(LinkedCardTypeBankCard())
            } else {
                SCREEN_BANK_CARD to listOf(LinkedCardTypeWallet())
            }
            is UnbindCard.Action.UnbindFailed -> ACTION_UNBIND_CARD to listOf(ActionUnbindCardStatusFail())
            is UnbindCard.Action.UnbindSuccess -> ACTION_UNBIND_CARD to listOf(ActionUnbindCardStatusSuccess())
            else -> null to null
        }

        name?.let { reporter.report(it, args) }
        return businessLogic(state, action)
    }
}