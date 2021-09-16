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
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindBusinessLogic
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCard
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardUseCase
import ru.yoomoney.sdk.march.Effect

class UnbindCardBusinessLogicEffectTest {

    private val showState: ShowState = mock()
    private val showEffect: ShowEffect = mock()
    private val source: Source = mock()
    private val unbindCardUseCase: UnbindCardUseCase = mock()

    private val logic = UnbindBusinessLogic(
        showState = { showState(it) },
        showEffect = { showEffect(it) },
        source = { source() },
        unbindCardUseCase = unbindCardUseCase
    )

    private val paymentInstrumentBankCard = PaymentInstrumentBankCard(
        paymentInstrumentId = "paymentInstrumentId",
        last4 = "0000",
        first6 = "000000",
        cscRequired = true,
        cardType = CardBrand.BANK_CARD
    )

    @Test
    fun `should effect state for UnbindCard state on UnbindSuccess action`() {
        // given
        val expected = UnbindCard.Effect.UnbindComplete(paymentInstrumentBankCard)
        val out = logic(
            state = UnbindCard.State.LoadingUnbinding(paymentInstrumentBankCard),
            action = UnbindCard.Action.UnbindSuccess
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    @Test
    fun `should show effect for Error state on UnbindFailed action`() {
        // given
        val expected = UnbindCard.Effect.UnbindFailed(paymentInstrumentBankCard)
        val out = logic(
            state = UnbindCard.State.LoadingUnbinding(paymentInstrumentBankCard),
            action = UnbindCard.Action.UnbindFailed
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

    private interface ShowState : (UnbindCard.State) -> UnbindCard.Action
    private interface ShowEffect : (UnbindCard.Effect) -> Unit
    private interface Source : () -> UnbindCard.Action

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