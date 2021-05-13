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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import ru.yoomoney.sdk.kassa.payments.metrics.AuthPaymentStatusFail
import ru.yoomoney.sdk.kassa.payments.metrics.AuthPaymentStatusSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out

private const val PAYMENT_AUTH_ACTION = "actionPaymentAuthorization"

internal class PaymentAuthAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<PaymentAuth.State, PaymentAuth.Action>
) : Logic<PaymentAuth.State, PaymentAuth.Action> {

    override fun invoke(
        state: PaymentAuth.State,
        action: PaymentAuth.Action
    ): Out<PaymentAuth.State, PaymentAuth.Action> {
        val (name, args) = when (action) {
            PaymentAuth.Action.ProcessAuthSuccess -> PAYMENT_AUTH_ACTION to listOf(AuthPaymentStatusSuccess())
            is PaymentAuth.Action.ProcessAuthWrongAnswer -> PAYMENT_AUTH_ACTION to listOf(AuthPaymentStatusFail())
            is PaymentAuth.Action.ProcessAuthFailed -> PAYMENT_AUTH_ACTION to listOf(AuthPaymentStatusFail())
            else -> null to null
        }
        name?.let { reporter.report(it, args) }
        return businessLogic(state, action)
    }
}