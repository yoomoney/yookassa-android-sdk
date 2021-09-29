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
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInputModel
import ru.yoomoney.sdk.kassa.payments.tokenize.Tokenize.Action
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import java.math.BigDecimal
import java.util.Currency

@RunWith(Parameterized::class)
internal class TokenizeBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: Tokenize.State,
    val action: Action,
    val expected: Tokenize.State
) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val charge = Amount(BigDecimal.TEN, Currency.getInstance("RUB"))
            val paymentOptionInfo = NewCardInfo(
                number = "number",
                expirationMonth = "expirationMonth",
                expirationYear = "expirationYear",
                csc = "csc"
            )
            val tokenizeInputModel = TokenizePaymentOptionInputModel(
                paymentOptionId = 1,
                savePaymentMethod = true,
                savePaymentInstrument = true,
                confirmation = ExternalConfirmation,
                paymentOptionInfo = paymentOptionInfo,
                allowWalletLinking = true
            )

            val startState = Tokenize.State.Start
            val tokenizeState = Tokenize.State.Tokenize(tokenizeInputModel)
            val tokenizeErrorState = Tokenize.State.TokenizeError(tokenizeInputModel, Throwable("Error state"))


            val tokenOutputModel = TokenizeOutputModel(
                token = "token",
                option = BankCardPaymentOption(
                    id = 1,
                    charge = charge,
                    fee = null,
                    savePaymentMethodAllowed = true,
                    confirmationTypes = listOf(ConfirmationType.EXTERNAL),
                    paymentInstruments = emptyList(),
                    savePaymentInstrument = false,
                    icon = null,
                    title = null
                ),
                instrumentBankCard = null
            )

            val tokenizeAction = Action.Tokenize(tokenizeInputModel)
            val tokenizeSuccess = Action.TokenizeSuccess(tokenOutputModel)
            val tokenizeFailed = Action.TokenizeFailed(tokenizeErrorState.error)

            val paymentAuthRequired = Action.PaymentAuthRequired(charge = charge)
            val paymentAuthSuccess = Action.PaymentAuthSuccess
            val paymentAuthCancel = Action.PaymentAuthCancel

            return generateBusinessLogicTests<Tokenize.State, Action>(
                generateState = {
                    when (it) {
                        Tokenize.State.Start::class -> startState
                        Tokenize.State.Tokenize::class -> tokenizeState
                        Tokenize.State.TokenizeError::class -> tokenizeErrorState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.Tokenize::class -> tokenizeAction
                        Action.TokenizeSuccess::class -> tokenizeSuccess
                        Action.TokenizeFailed::class -> tokenizeFailed
                        Action.PaymentAuthRequired::class -> paymentAuthRequired
                        Action.PaymentAuthSuccess::class -> paymentAuthSuccess
                        Action.PaymentAuthCancel::class -> paymentAuthCancel
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        startState to tokenizeAction -> tokenizeState
                        tokenizeState to tokenizeSuccess -> tokenizeState
                        tokenizeState to tokenizeFailed -> tokenizeErrorState
                        tokenizeState to paymentAuthRequired -> tokenizeState
                        tokenizeState to paymentAuthSuccess -> tokenizeState
                        tokenizeState to paymentAuthCancel -> tokenizeState
                        tokenizeErrorState to tokenizeAction -> tokenizeState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = TokenizeBusinessLogic(mock(), mock(), mock(), mock())

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}