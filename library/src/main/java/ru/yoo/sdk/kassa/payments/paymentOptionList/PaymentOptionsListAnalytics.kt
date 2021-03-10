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

package ru.yoo.sdk.kassa.payments.paymentOptionList

import ru.yoo.sdk.kassa.payments.metrics.AuthType
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoo.sdk.march.Logic
import ru.yoo.sdk.march.Out

internal class PaymentOptionListAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<PaymentOptionList.State, PaymentOptionList.Action>,
    private val getUserAuthType: () -> AuthType,
    private val getTokenizeScheme: () -> TokenizeScheme?
):  Logic<PaymentOptionList.State, PaymentOptionList.Action> {

    override fun invoke(state: PaymentOptionList.State, action: PaymentOptionList.Action): Out<PaymentOptionList.State, PaymentOptionList.Action> {
        when(action) {
            is PaymentOptionList.Action.LoadPaymentOptionListFailed -> {
                val params = getTokenizeScheme()?.let { listOf(getUserAuthType(), it) } ?: listOf(getUserAuthType())
                reporter.report("screenError", params)
            }
            is PaymentOptionList.Action.LoadPaymentOptionListSuccess -> {
                reporter.report("screenPaymentOptions", listOf(getUserAuthType()))
            }
            else -> Unit
        }
        return businessLogic(state, action)
    }
}