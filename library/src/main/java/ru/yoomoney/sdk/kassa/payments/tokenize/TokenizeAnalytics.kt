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

package ru.yoomoney.sdk.kassa.payments.tokenize

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.metrics.AuthType
import ru.yoomoney.sdk.kassa.payments.metrics.ColorMetricsProvider
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SavePaymentMethodProvider
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.metrics.UserAttiributionOnInitProvider
import ru.yoomoney.sdk.kassa.payments.metrics.YooKassaIconProvider
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out

private const val ACTION_TRY_TOKENIZE = "actionTryTokenize"
private const val ACTION_TOKENIZE = "actionTokenize"

internal class TokenizeAnalytics(
    private val reporter: Reporter,
    private val errorScreenReporter: ErrorScreenReporter,
    private val businessLogic: Logic<Tokenize.State, Tokenize.Action>,
    private val getUserAuthType: () -> AuthType,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val getTokenizeScheme: () -> TokenizeScheme?,
    private val paymentParameters: PaymentParameters,
    private val uiParameters: UiParameters
) : Logic<Tokenize.State, Tokenize.Action> {

    override fun invoke(state: Tokenize.State, action: Tokenize.Action): Out<Tokenize.State, Tokenize.Action> {
        val nameArgsPairs = when (action) {
            is Tokenize.Action.Tokenize -> {
                getTokenizeScheme()?.let { tokenizeScheme ->
                    listOf(
                        ACTION_TRY_TOKENIZE to listOf(
                            getUserAuthType(),
                            tokenizeScheme,
                            SavePaymentMethodProvider().invoke(paymentParameters),
                            YooKassaIconProvider().invoke(uiParameters),
                            ColorMetricsProvider().invoke(uiParameters),
                            UserAttiributionOnInitProvider(userAuthInfoRepository).invoke(paymentParameters)
                        )
                    )
                } ?: listOf(null to null)
            }
            is Tokenize.Action.TokenizeSuccess -> {
                val tokenizeScheme = getTokenizeScheme()
                if (action.content is TokenizeOutputModel && tokenizeScheme != null) {
                    listOf(
                        ACTION_TOKENIZE to listOf(tokenizeScheme, getUserAuthType())
                    )
                } else {
                    listOf(null to null)
                }
            }
            else -> listOf(null to null)
        }

        nameArgsPairs.forEach { pair ->
            pair.first?.let {
                reporter.report(it, pair.second)
            }
        }

        errorScreenReporter.takeIf { action is Tokenize.Action.TokenizeFailed }?.report()

        return businessLogic(state, action)
    }
}