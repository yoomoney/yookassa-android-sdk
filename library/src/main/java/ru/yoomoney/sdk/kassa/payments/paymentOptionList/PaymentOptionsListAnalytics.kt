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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import ru.yoomoney.sdk.kassa.payments.metrics.ActionUnbindCardStatusFail
import ru.yoomoney.sdk.kassa.payments.metrics.ActionUnbindCardStatusSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.AuthType
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out

private const val ACTION_UNBIND_CARD = "actionUnbindBankCard"
private const val ACTION_SCREEN_ERROR = "screenError"
private const val ACTION_SCREEN_PAYMENT_OPTIONS = "screenPaymentOptions"

internal class PaymentOptionListAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<PaymentOptionList.State, PaymentOptionList.Action>,
    private val getUserAuthType: () -> AuthType,
    private val getTokenizeScheme: () -> TokenizeScheme?
):  Logic<PaymentOptionList.State, PaymentOptionList.Action> {

    override fun invoke(
        state: PaymentOptionList.State,
        action: PaymentOptionList.Action)
    : Out<PaymentOptionList.State, PaymentOptionList.Action> {
        when(action) {
            is PaymentOptionList.Action.LoadPaymentOptionListFailed -> {
                val params = getTokenizeScheme()?.let { listOf(getUserAuthType(), it) } ?: listOf(getUserAuthType())
                reporter.report(ACTION_SCREEN_ERROR, params)
            }
            is PaymentOptionList.Action.LoadPaymentOptionListSuccess -> {
                reporter.report(ACTION_SCREEN_PAYMENT_OPTIONS, listOf(getUserAuthType()))
            }
            is PaymentOptionList.Action.UnbindFailed -> {
                reporter.report(ACTION_UNBIND_CARD, listOf(ActionUnbindCardStatusFail()))
            }
            is PaymentOptionList.Action.UnbindSuccess -> {
                reporter.report(ACTION_UNBIND_CARD, listOf(ActionUnbindCardStatusSuccess()))
            }
            else -> Unit
        }
        return businessLogic(state, action)
    }
}