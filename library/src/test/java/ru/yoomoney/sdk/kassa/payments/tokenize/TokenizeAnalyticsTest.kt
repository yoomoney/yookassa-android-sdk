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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTokenTypeSingle
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTypeYooMoneyLogin
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporterImpl
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeSberPay
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import java.math.BigDecimal

class TokenizeAnalyticsTest {
    private val reporter: Reporter = mock()

    private val userAuthType = AuthTypeYooMoneyLogin()
    private val userAuthTokenType = AuthTokenTypeSingle()
    private val errorScreenReporter = ErrorScreenReporterImpl(
        reporter = reporter,
        getAuthType = { userAuthType },
        getTokenizeScheme = { null }
    )
    private val getTokenizeScheme: (PaymentOption, PaymentInstrumentBankCard?) -> TokenizeScheme = mock()

    private val tokenizeAnalytics = TokenizeAnalytics(
        reporter = reporter,
        errorScreenReporter = errorScreenReporter,
        businessLogic = mock(),
        getUserAuthType = { userAuthType },
        getTokenizeScheme = getTokenizeScheme,
        getUserAuthTokenType = { userAuthTokenType }
    )

    @Test
    fun `verify actionTokenize analytics sends`() {
        // given
        val wallet = Wallet(
            id = 1,
            charge = Amount(BigDecimal("3.00"), RUB),
            fee = null,
            icon = null,
            title = null,
            walletId = "123456789",
            balance = Amount(BigDecimal("5.00"), RUB),
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false
        )

        whenever(getTokenizeScheme(wallet, null)).thenReturn(TokenizeSchemeSberPay())

        // when
        tokenizeAnalytics(
            Tokenize.State.Start,
            Tokenize.Action.TokenizeSuccess(
                TokenizeOutputModel(
                    "token",
                    wallet,
                    null
                )
            )
        )

        // then
        verify(reporter).report(
            "actionTokenize", listOf(
                TokenizeSchemeSberPay(),
                userAuthType,
                userAuthTokenType
            )
        )
    }
}