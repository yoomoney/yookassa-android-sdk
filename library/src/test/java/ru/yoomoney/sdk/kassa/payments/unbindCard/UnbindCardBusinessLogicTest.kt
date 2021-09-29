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

package ru.yoomoney.sdk.kassa.payments.unbindCard

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindBusinessLogic
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCard
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import java.math.BigDecimal

@RunWith(Parameterized::class)
internal class UnbindCardBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: UnbindCard.State,
    val action: UnbindCard.Action,
    val expected: UnbindCard.State
) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {

            val instrumentId = "paymentInstrumentId"

            val paymentInstrumentBankCard = PaymentInstrumentBankCard(
                paymentInstrumentId = instrumentId,
                last4 = "0000",
                first6 = "000000",
                cscRequired = true,
                cardType = CardBrand.BANK_CARD
            )

            val linkedCard = LinkedCard(
                id = 1,
                charge = Amount(BigDecimal.TEN, RUB),
                fee = Fee(
                    Amount(BigDecimal.ONE, RUB),
                    Amount(BigDecimal("0.5"), RUB)
                ),
                icon = null,
                title = null,
                cardId = "12345654321",
                brand = CardBrand.MASTER_CARD,
                pan = "1234567887654321",
                savePaymentMethodAllowed = true,
                confirmationTypes = listOf(ConfirmationType.REDIRECT),
                savePaymentInstrument = false
            )

            val initialState = UnbindCard.State.Initial
            val unbindLinkedCardState = UnbindCard.State.ContentLinkedWallet(linkedCard)
            val unbindBankCardState = UnbindCard.State.ContentLinkedBankCard(paymentInstrumentBankCard)
            val unbindLoadingState = UnbindCard.State.LoadingUnbinding(paymentInstrumentBankCard)

            val startDisplayDataAction = UnbindCard.Action.StartDisplayData(linkedCard, paymentInstrumentBankCard)
            val unbindCardAction = UnbindCard.Action.StartUnbinding(instrumentId)
            val unbindFailAction = UnbindCard.Action.UnbindFailed
            val unbindSuccessAction = UnbindCard.Action.UnbindSuccess

            return generateBusinessLogicTests<UnbindCard.State, UnbindCard.Action>(
                generateState = {
                    when (it) {
                        UnbindCard.State.Initial::class -> initialState
                        UnbindCard.State.ContentLinkedWallet::class -> unbindLinkedCardState
                        UnbindCard.State.ContentLinkedBankCard::class -> unbindBankCardState
                        UnbindCard.State.LoadingUnbinding::class -> unbindLoadingState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        UnbindCard.Action.StartUnbinding::class -> unbindCardAction
                        UnbindCard.Action.UnbindFailed::class -> unbindFailAction
                        UnbindCard.Action.UnbindSuccess::class -> unbindSuccessAction
                        UnbindCard.Action.StartDisplayData::class -> startDisplayDataAction
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        initialState to startDisplayDataAction -> unbindLinkedCardState
                        unbindBankCardState to unbindCardAction -> unbindLoadingState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic =
        UnbindBusinessLogic(
            showState = mock(),
            source = mock(),
            showEffect = mock(),
            unbindCardUseCase = mock()
        )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertThat(actual.state, equalTo(expected))
    }
}