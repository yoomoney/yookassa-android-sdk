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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInputModel
import ru.yoomoney.sdk.kassa.payments.utils.func
import java.math.BigDecimal
import java.util.Currency

@ExperimentalCoroutinesApi
class TokenizeBusinessLogicEffectTest {

    private val showState: (Tokenize.State) -> Tokenize.Action = mock()
    private val showEffect: (Tokenize.Effect) -> Unit = mock()
    private val source: () -> Tokenize.Action = mock()
    private val useCase: TokenizeUseCase = mock()

    private val charge = Amount(BigDecimal.TEN, Currency.getInstance("RUB"))
    private val paymentOptionInfo = NewCardInfo(
        number = "number",
        expirationMonth = "expirationMonth",
        expirationYear = "expirationYear",
        csc = "csc"
    )
    private val tokenizeInputModel = TokenizePaymentOptionInputModel(
        paymentOptionId = 1,
        savePaymentMethod = true,
        savePaymentInstrument = true,
        confirmation = ExternalConfirmation,
        paymentOptionInfo = paymentOptionInfo,
        allowWalletLinking = true
    )
    private val tokenOutputModel = TokenizeOutputModel(
        token = "token",
        option = BankCardPaymentOption(
            id = 1,
            charge = charge,
            fee = null,
            icon = null,
            title = null,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.EXTERNAL),
            paymentInstruments = emptyList(),
            savePaymentInstrument = false
        ),
        instrumentBankCard = null
    )
    private val error = Throwable("Test error")

    private val logic = TokenizeBusinessLogic(
        showState = { showState(it) },
        showEffect = { showEffect(it) },
        source = { source() },
        tokenizeUseCase = useCase
    )

    @Test
    fun `should show Tokenize state and start tokenize from Start state on Tokenize action`() = runBlockingTest {
        // given
        val expected = Tokenize.State.Tokenize(tokenizeInputModel)
        val out = logic(Tokenize.State.Start, Tokenize.Action.Tokenize(tokenizeInputModel))

        // when
        out.sources.func()

        // then
        verify(showState).invoke(expected)
        verify(useCase).tokenize(tokenizeInputModel)
    }

    @Test
    fun `should show show PaymentAuthRequired effect from Tokenize state on PaymentAuthRequired action`() {
        // given
        val expected = Tokenize.Effect.PaymentAuthRequired(charge, true)
        val out = logic(Tokenize.State.Tokenize(tokenizeInputModel), Tokenize.Action.PaymentAuthRequired(charge))

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
    }

    @Test
    fun `should start tokenize from Tokenize state on PaymentAuthSuccess action`() = runBlockingTest {
        // given
        val out = logic(Tokenize.State.Tokenize(tokenizeInputModel), Tokenize.Action.PaymentAuthSuccess)

        // when
        out.sources.func()

        // then
        verify(useCase).tokenize(tokenizeInputModel)
    }

    @Test
    fun `should show show CancelTokenize effect from Tokenize state on PaymentAuthCancel action`() {
        // given
        val expected = Tokenize.Effect.CancelTokenize
        val state = Tokenize.State.Tokenize(tokenizeInputModel)
        val out = logic(state, Tokenize.Action.PaymentAuthCancel)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showState).invoke(state)
        verify(showEffect).invoke(expected)
    }

    @Test
    fun `should show show TokenizeComplete effect from Tokenize state on TokenizeSuccess action`() {
        // given
        val expected = Tokenize.Effect.TokenizeComplete(tokenOutputModel, true)
        val out = logic(Tokenize.State.Tokenize(tokenizeInputModel), Tokenize.Action.TokenizeSuccess(tokenOutputModel))

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
    }

    @Test
    fun `should show show TokenizeError state from Tokenize state on TokenizeFailed action`() {
        // given
        val expected = Tokenize.State.TokenizeError(tokenizeInputModel, error)
        val out = logic(Tokenize.State.Tokenize(tokenizeInputModel), Tokenize.Action.TokenizeFailed(error))

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showState).invoke(expected)
    }

    @Test
    fun `should show Tokenize state and start tokenize from TokenizeError state on Tokenize action`() = runBlockingTest {
        // given
        val expected = Tokenize.State.Tokenize(tokenizeInputModel)
        val out = logic(Tokenize.State.TokenizeError(tokenizeInputModel, error), Tokenize.Action.Tokenize(tokenizeInputModel))

        // when
        out.sources.func()

        // then
        verify(showState).invoke(expected)
        verify(useCase).tokenize(tokenizeInputModel)
    }
}