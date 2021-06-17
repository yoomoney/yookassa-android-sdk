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

package ru.yoomoney.sdk.kassa.payments.contract

import ru.yoomoney.sdk.kassa.payments.metrics.AuthTokenType
import ru.yoomoney.sdk.kassa.payments.metrics.AuthType
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.NewCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption

internal class ContractAnalytics(
    private val reporter: Reporter,
    private val errorScreenReporter: ErrorScreenReporter,
    private val businessLogic: Logic<Contract.State, Contract.Action>,
    private val getUserAuthType: () -> AuthType,
    private val getUserAuthTokenType: () -> AuthTokenType,
    private val getTokenizeScheme: (PaymentOption) -> TokenizeScheme
) : Logic<Contract.State, Contract.Action> {

    override fun invoke(state: Contract.State, action: Contract.Action): Out<Contract.State, Contract.Action> {
        val nameArgsPairs = when (action) {
            Contract.Action.Logout -> listOf("actionLogout" to null)
            is Contract.Action.TokenizeSuccess -> {
                if (action.content is TokenOutputModel) {
                    listOf(
                        "actionTokenize" to listOf(
                            getTokenizeScheme(action.content.option),
                            getUserAuthType(),
                            getUserAuthTokenType()
                        )
                    )
                } else {
                    listOf(null to null)
                }
            }
            is Contract.Action.LoadContractSuccess -> {
                listOf(
                    "screenPaymentContract" to listOf(
                        getUserAuthType(),
                        getTokenizeScheme(action.outputModel.paymentOption)
                    ),
                    when (action.outputModel.paymentOption) {
                        is NewCard -> "screenBankCardForm" to listOf(getUserAuthType())
                        is LinkedCard -> "screenLinkedCardForm" to null
                        is PaymentIdCscConfirmation -> "screenRecurringCardForm" to null
                        else -> null to null
                    }
                )
            }
            else -> listOf(null to null)
        }

        nameArgsPairs.forEach { pair ->
            pair.first?.let {
                reporter.report(it, pair.second)
            }
        }

        errorScreenReporter.takeIf { action is Contract.Action.LoadContractFailed }?.report()

        return businessLogic(state, action)
    }
}