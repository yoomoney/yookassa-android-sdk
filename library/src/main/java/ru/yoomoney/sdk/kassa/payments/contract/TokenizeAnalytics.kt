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

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.metrics.AuthType
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SavePaymentMethodProvider
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption

private const val ACTION_SCREEN_PAYMENT_CONTRACT = "screenPaymentContract"
private const val ACTION_SCREEN_PAYMENT_CONTRACT_ERROR = "screenErrorContract"
private const val ACTION_LOGOUT = "actionLogout"

internal class ContractAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<Contract.State, Contract.Action>,
    private val getUserAuthType: () -> AuthType,
    private val paymentParameters: PaymentParameters,
    private val tokenizeSchemeParamProvider: TokenizeSchemeParamProvider,
    private val getTokenizeScheme: (PaymentOption, PaymentInstrumentBankCard?) -> TokenizeScheme
) : Logic<Contract.State, Contract.Action> {

    override fun invoke(state: Contract.State, action: Contract.Action): Out<Contract.State, Contract.Action> {
        val nameArgsPairs = when (action) {
            Contract.Action.Logout -> listOf(ACTION_LOGOUT to null)
            is Contract.Action.LoadContractSuccess -> {
                listOf(
                    ACTION_SCREEN_PAYMENT_CONTRACT to listOf(
                        getUserAuthType(),
                        getTokenizeScheme(
                            action.outputModel.paymentOption,
                            action.outputModel.instrument
                        )
                    )
                )
            }
            is Contract.Action.LoadContractFailed -> {
                tokenizeSchemeParamProvider.invoke()?.let { tokenizeScheme ->
                    listOf(
                        ACTION_SCREEN_PAYMENT_CONTRACT_ERROR to listOf(
                            getUserAuthType(),
                            tokenizeScheme,
                            SavePaymentMethodProvider().invoke(paymentParameters)
                        )
                    )
                }
            }
            else -> listOf(null to null)
        }

        nameArgsPairs?.forEach { pair ->
            pair.first?.let {
                reporter.report(it, pair.second)
            }
        }

        return businessLogic(state, action)
    }
}