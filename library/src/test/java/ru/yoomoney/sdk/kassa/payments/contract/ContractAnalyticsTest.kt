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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okio.ByteString.Companion.decodeHex
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTypeYooMoneyLogin
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporterImpl
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeLinkedCard
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import java.math.BigDecimal

class ContractAnalyticsTest {
    private val reporter: Reporter = mock()

    private val userAuthType = AuthTypeYooMoneyLogin()
    private val errorScreenReporter = ErrorScreenReporterImpl(
        reporter = reporter,
        getAuthType = { userAuthType },
        getTokenizeScheme = { null }
    )
    private val getTokenizeScheme: (PaymentOption, PaymentInstrumentBankCard?) -> TokenizeScheme = mock()

    private val contractAnalytics = ContractAnalytics(
        reporter = reporter,
        errorScreenReporter = errorScreenReporter,
        businessLogic = mock(),
        getUserAuthType = { userAuthType },
        getTokenizeScheme = getTokenizeScheme
    )

    @Test
    fun `verify money auth success analytics sends`() {
        // given

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.Logout
        )

        // then
        verify(reporter).report("actionLogout")
    }

    @Test
    fun `verify screenBankCardForm analytics sends`() {
        // given
        val newCard = BankCardPaymentOption(
            id = 0,
            charge = Amount(BigDecimal("2.00"), RUB),
            fee = null,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false,
            paymentInstruments = emptyList(),
            title = null,
            icon = null
        )

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.LoadContractSuccess(
                SelectedPaymentMethodOutputModel(newCard, instrument = null, walletLinkingPossible = false)
            )
        )

        // then
        verify(reporter).report("screenBankCardForm", listOf(userAuthType))
    }

    @Test
    fun `verify screenLinkedCardForm analytics sends`() {
        // given
        val linkedCard = LinkedCard(
            id = 1,
            charge = Amount(BigDecimal.TEN, RUB),
            fee = Fee(
                Amount(BigDecimal.ONE, RUB),
                Amount(BigDecimal("0.5"), RUB)
            ),
            title = null,
            icon = null,
            cardId = "12345654321",
            brand = CardBrand.MASTER_CARD,
            pan = "1234567887654321",
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false
        )

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.LoadContractSuccess(
                SelectedPaymentMethodOutputModel(linkedCard, null, false)
            )
        )

        // then
        verify(reporter).report("screenLinkedCardForm")
    }

    @Test
    fun `verify screenRecurringCardForm analytics sends`() {
        // given
        val cscConfirmation = PaymentIdCscConfirmation(
            id = 0,
            charge = Amount(BigDecimal.TEN, RUB),
            paymentMethodId = "123",
            fee = null,
            first = "123456",
            last = "7890",
            expiryYear = "2020",
            expiryMonth = "12",
            title = null,
            icon = null,
            savePaymentMethodAllowed = true,
            confirmationTypes = emptyList(),
            brand = CardBrand.MASTER_CARD,
            savePaymentInstrument = false
        )

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.LoadContractSuccess(
                SelectedPaymentMethodOutputModel(cscConfirmation, null, false)
            )
        )

        // then
        verify(reporter).report("screenRecurringCardForm")
    }

    @Test
    fun `verify error screen analytics sends`() {
        // given

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.LoadContractFailed(SdkException())
        )

        // then
        verify(reporter).report("screenError", listOf(userAuthType))
    }

    @Test
    fun `verify screenPaymentContract analytics sends`() {
        // given
        val linkedCard = LinkedCard(
            id = 1,
            charge = Amount(BigDecimal.TEN, RUB),
            fee = Fee(
                Amount(BigDecimal.ONE, RUB),
                Amount(BigDecimal("0.5"), RUB)
            ),
            cardId = "12345654321",
            brand = CardBrand.MASTER_CARD,
            pan = "1234567887654321",
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false,
            icon = null,
            title = null
        )

        whenever(getTokenizeScheme(linkedCard, null)).thenReturn(TokenizeSchemeLinkedCard())

        // when
        contractAnalytics(
            Contract.State.Loading,
            Contract.Action.LoadContractSuccess(
                SelectedPaymentMethodOutputModel(linkedCard, null, false)
            )
        )

        // then
        verify(reporter).report("screenPaymentContract", listOf(userAuthType,
            TokenizeSchemeLinkedCard()
        ))
    }
}