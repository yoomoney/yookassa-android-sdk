/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.impl.paymentAuth

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Action
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.State
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthBusinessLogic
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import java.math.BigDecimal
import java.util.Currency

@RunWith(Parameterized::class)
internal class PaymentAuthBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: State,
    val action: Action,
    val expected: State
) {
    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val failure = SdkException()

            val loadingState = State.Loading
            val loadingErrorState = State.StartError(failure)
            val inputCodeState = State.InputCode(AuthTypeState.SMS(
                nextSessionTimeLeft = 10,
                codeLength = 4,
                attemptsCount = 3,
                attemptsLeft = 2
            ))

            val inputCodeProcessState = State.InputCodeProcess("passphrase", AuthTypeState.SMS(
                nextSessionTimeLeft = 10,
                codeLength = 4,
                attemptsCount = 3,
                attemptsLeft = 2
            ))

            val inputCodeVerifyExceededState = State.InputCodeVerifyExceeded("passphrase", AuthTypeState.SMS(
                nextSessionTimeLeft = 10,
                codeLength = 4,
                attemptsCount = 3,
                attemptsLeft = 2
            ))

            val processErrorState = State.ProcessError(
                AuthTypeState.SMS(
                    nextSessionTimeLeft = 10,
                    codeLength = 4,
                    attemptsCount = 3,
                    attemptsLeft = 2
                ),
                failure
            )

            val startAction = Action.Start(true,
                Amount(
                    BigDecimal.TEN,
                    Currency.getInstance("RUB")
                )
            )
            val startSuccessAction = Action.StartSuccess(AuthTypeState.SMS(
                nextSessionTimeLeft = 10,
                codeLength = 4,
                attemptsCount = 3,
                attemptsLeft = 2
            ))
            val startFailAction = Action.StartFailed(failure)

            val processAuthRequiredAction = Action.ProcessAuthRequired("passphrase", true)
            val processAuthWrongAnswerAction = Action.ProcessAuthWrongAnswer(AuthTypeState.SMS(
                nextSessionTimeLeft = 10,
                codeLength = 4,
                attemptsCount = 3,
                attemptsLeft = 2
            ))
            val processAuthNotRequiredAction = Action.ProcessAuthNotRequired(true)
            val processAuthSuccessAction = Action.ProcessAuthSuccess
            val processAuthFailed = Action.ProcessAuthFailed(failure)
            val processAuthSessionBroken = Action.ProcessAuthSessionBroken(loadingErrorState.error)

            return generateBusinessLogicTests<State, Action>(
                generateState = {
                    when (it) {
                        State.Loading::class -> loadingState
                        State.StartError::class -> loadingErrorState
                        State.InputCode::class -> inputCodeState
                        State.InputCodeProcess::class -> inputCodeProcessState
                        State.InputCodeVerifyExceeded::class -> inputCodeVerifyExceededState
                        State.ProcessError::class -> processErrorState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.Start::class -> startAction
                        Action.StartSuccess::class -> startSuccessAction
                        Action.StartFailed::class -> startFailAction
                        Action.ProcessAuthRequired::class -> processAuthRequiredAction
                        Action.ProcessAuthWrongAnswer::class -> processAuthWrongAnswerAction
                        Action.ProcessAuthNotRequired::class -> processAuthNotRequiredAction
                        Action.ProcessAuthVerifyExceeded::class -> Action.ProcessAuthVerifyExceeded
                        Action.ProcessAuthSuccess::class -> processAuthSuccessAction
                        Action.ProcessAuthFailed::class -> processAuthFailed
                        Action.ProcessAuthSessionBroken::class -> processAuthSessionBroken
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        loadingState to startAction -> loadingState
                        loadingState to startSuccessAction -> inputCodeState
                        loadingState to startFailAction -> loadingErrorState
                        loadingState to processAuthRequiredAction -> loadingState
                        loadingState to processAuthFailed -> loadingErrorState
                        loadingErrorState to startAction -> loadingState

                        inputCodeState to startAction -> loadingState
                        inputCodeState to processAuthRequiredAction -> inputCodeProcessState
                        inputCodeState to processAuthWrongAnswerAction -> inputCodeState
                        inputCodeState to processAuthRequiredAction -> inputCodeProcessState

                        inputCodeProcessState to processAuthFailed -> processErrorState
                        inputCodeProcessState to processAuthWrongAnswerAction -> inputCodeState
                        inputCodeProcessState to Action.ProcessAuthVerifyExceeded -> inputCodeVerifyExceededState
                        inputCodeProcessState to processAuthSessionBroken -> loadingErrorState
                        processErrorState to startSuccessAction -> inputCodeState
                        inputCodeVerifyExceededState to startAction -> loadingState

                        else -> state
                    }
                }
            )
        }
    }

    private val logic = PaymentAuthBusinessLogic(
        mock(),
        mock(),
        mock(),
        mock(),
        mock()
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}