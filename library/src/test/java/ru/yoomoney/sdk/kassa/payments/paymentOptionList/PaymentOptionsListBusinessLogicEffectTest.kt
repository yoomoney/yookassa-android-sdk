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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.ShopProperties
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInstrumentInputModel
import ru.yoomoney.sdk.march.Effect
import java.math.BigDecimal

internal class PaymentOptionsListBusinessLogicEffectTest {

    private val showState: (PaymentOptionList.State) -> PaymentOptionList.Action = mock()
    private val showEffect: (PaymentOptionList.Effect) -> Unit = mock()
    private val source: () -> PaymentOptionList.Action = mock()
    private val useCase: PaymentOptionsListUseCase = mock()
    private val contentState = PaymentOptionList.State.Content(PaymentOptionListSuccessOutputModel(listOf()))
    private val shopPropertiesRepository: ShopPropertiesRepository = mock()

    @Test
    fun `Should send ProceedWithPaymentMethod effect with Content state and ProceedWithPaymentMethod action`() {
        // given
        val paymentOptionId = 11
        val instrumentId = "instrumentId"
        val expected = PaymentOptionList.Effect.ShowContract
        val out = createLogic().invoke(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId, instrumentId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    @Test
    fun `Should show StartTokenization effect with Content state and ProceedWithPaymentMethod action`() {
        // given
        val paymentOptionId = 11
        val instrumentId = "instrumentId"
        val paymentInstrument = PaymentInstrumentBankCard(
            paymentInstrumentId = instrumentId,
            last4 = "last4",
            first6 = "first6",
            cscRequired = false,
            cardType = CardBrand.MASTER_CARD
        )
        val expected = PaymentOptionList.Effect.StartTokenization(
            TokenizeInstrumentInputModel(
                paymentOptionId = paymentOptionId,
                savePaymentMethod = false,
                instrumentBankCard = paymentInstrument,
                allowWalletLinking = false,
                confirmation = NoConfirmation,
                csc = null
            )
        )

        val getConfirmation: GetConfirmation = mock()
        whenever(getConfirmation(any())).thenReturn(NoConfirmation)

        whenever(useCase.selectPaymentOption(paymentOptionId, instrumentId)).thenReturn(
            BankCardPaymentOption(
                id = paymentOptionId,
                charge = Amount(BigDecimal.TEN, RUB),
                fee = null,
                savePaymentMethodAllowed = true,
                confirmationTypes = emptyList(),
                paymentInstruments = listOf(paymentInstrument),
                savePaymentInstrument = true
            )
        )
        whenever(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(false, false))

        val paymentParameters = cratePaymentParameters(savePaymentMethod = SavePaymentMethod.OFF)
        val out = createLogic(paymentParameters = paymentParameters, getConfirmation = getConfirmation).invoke(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId, instrumentId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    @Test
    fun `Should show ShowContract effect with Content state and ProceedWithPaymentMethod action because of cscRequired is true`() {
        // given
        val paymentOptionId = 11
        val instrumentId = "instrumentId"
        val paymentInstrument = PaymentInstrumentBankCard(
            paymentInstrumentId = instrumentId,
            last4 = "last4",
            first6 = "first6",
            cscRequired = true,
            cardType = CardBrand.MASTER_CARD
        )
        val expected = PaymentOptionList.Effect.ShowContract

        val getConfirmation: GetConfirmation = mock()
        whenever(getConfirmation(any())).thenReturn(NoConfirmation)

        whenever(useCase.selectPaymentOption(paymentOptionId, instrumentId)).thenReturn(
            BankCardPaymentOption(
                id = paymentOptionId,
                charge = Amount(BigDecimal.TEN, RUB),
                fee = null,
                savePaymentMethodAllowed = true,
                confirmationTypes = emptyList(),
                paymentInstruments = listOf(paymentInstrument),
                savePaymentInstrument = true
            )
        )
        whenever(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(false, false))

        val paymentParameters = cratePaymentParameters(savePaymentMethod = SavePaymentMethod.OFF)
        val out = createLogic(paymentParameters = paymentParameters, getConfirmation = getConfirmation).invoke(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId, instrumentId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    @Test
    fun `Should show ShowContract effect with Content state and ProceedWithPaymentMethod action because of SavePaymentMethod_ON`() {
        // given
        val paymentOptionId = 11
        val instrumentId = "instrumentId"
        val paymentInstrument = PaymentInstrumentBankCard(
            paymentInstrumentId = instrumentId,
            last4 = "last4",
            first6 = "first6",
            cscRequired = false,
            cardType = CardBrand.MASTER_CARD
        )
        val expected = PaymentOptionList.Effect.ShowContract

        val getConfirmation: GetConfirmation = mock()
        whenever(getConfirmation(any())).thenReturn(NoConfirmation)

        whenever(useCase.selectPaymentOption(paymentOptionId, instrumentId)).thenReturn(
            BankCardPaymentOption(
                id = paymentOptionId,
                charge = Amount(BigDecimal.TEN, RUB),
                fee = null,
                savePaymentMethodAllowed = true,
                confirmationTypes = emptyList(),
                paymentInstruments = listOf(paymentInstrument),
                savePaymentInstrument = true
            )
        )
        whenever(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(false, false))

        val paymentParameters = cratePaymentParameters(savePaymentMethod = SavePaymentMethod.ON)
        val out = createLogic(paymentParameters = paymentParameters, getConfirmation = getConfirmation).invoke(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId, instrumentId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    @Test
    fun `Should show ShowContract effect with Content state and ProceedWithPaymentMethod action because of fee`() {
        // given
        val paymentOptionId = 11
        val instrumentId = "instrumentId"
        val paymentInstrument = PaymentInstrumentBankCard(
            paymentInstrumentId = instrumentId,
            last4 = "last4",
            first6 = "first6",
            cscRequired = false,
            cardType = CardBrand.MASTER_CARD
        )
        val expected = PaymentOptionList.Effect.ShowContract

        val getConfirmation: GetConfirmation = mock()
        whenever(getConfirmation(any())).thenReturn(NoConfirmation)

        whenever(useCase.selectPaymentOption(paymentOptionId, instrumentId)).thenReturn(
            BankCardPaymentOption(
                id = paymentOptionId,
                charge = Amount(BigDecimal.TEN, RUB),
                fee = Fee(service = Amount(BigDecimal.ONE, RUB)),
                savePaymentMethodAllowed = true,
                confirmationTypes = emptyList(),
                paymentInstruments = listOf(paymentInstrument),
                savePaymentInstrument = true
            )
        )
        whenever(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(false, false))

        val paymentParameters = cratePaymentParameters(savePaymentMethod = SavePaymentMethod.OFF)
        val out = createLogic(paymentParameters = paymentParameters, getConfirmation = getConfirmation).invoke(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId, instrumentId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    private fun createLogic(
        paymentParameters: PaymentParameters = cratePaymentParameters(),
        getConfirmation: GetConfirmation = mock()
    ) = PaymentOptionsListBusinessLogic(
        showState = { showState(it) },
        showEffect = { showEffect(it) },
        source = { source() },
        useCase = useCase,
        paymentParameters = paymentParameters,
        logoutUseCase = mock(),
        getConfirmation = getConfirmation,
        unbindCardUseCase = mock(),
        shopPropertiesRepository = shopPropertiesRepository
    )

    private fun cratePaymentParameters(
        amount: Amount = Amount(BigDecimal.ONE, RUB),
        title: String = "title",
        subtitle: String = "subtitle",
        clientApplicationKey: String = "clientApplicationKey",
        shopId: String = "shopId",
        savePaymentMethod: SavePaymentMethod = SavePaymentMethod.ON,
        authCenterClientId: String = "authCenterClientId"
    ) = PaymentParameters(
        amount = amount,
        title = title,
        subtitle = subtitle,
        clientApplicationKey = clientApplicationKey,
        shopId = shopId,
        savePaymentMethod = savePaymentMethod,
        authCenterClientId = authCenterClientId
    )

    private suspend fun <E> List<Effect<E>>.func() {
        forEach {
            when (it) {
                is Effect.Input.Fun -> it.func()
                is Effect.Output -> it.func()
                else -> error("unexpected")
            }
        }
    }
}