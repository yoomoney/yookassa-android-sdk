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

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Action
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.State
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListSuccessOutputModel
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListBusinessLogic
import java.math.BigDecimal
import java.util.Currency

@RunWith(Parameterized::class)
internal class PaymentOptionsListBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: State,
    val action: Action,
    val expected: State
) {
    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val paymentOptionId = 11
            val contentData = PaymentOptionListSuccessOutputModel(listOf())

            val failure = SdkException()
            val loadingState = State.Loading
            val errorState = State.Error(failure)
            val contentState = State.Content(contentData)
            val waitingForAuthState = State.WaitingForAuthState(contentState)

            val successDataAction = Action.LoadPaymentOptionListSuccess(contentData)
            val failAction = Action.LoadPaymentOptionListFailed(failure)
            val loadAction = Action.Load(
                Amount(
                    BigDecimal.TEN,
                    Currency.getInstance("RUB")
                ), null)
            val logoutAction = Action.Logout
            val proceedWithPaymentMethodAction = Action.ProceedWithPaymentMethod(paymentOptionId)
            val successAuth = Action.PaymentAuthSuccess
            val cancelAuth = Action.PaymentAuthCancel

            return generateBusinessLogicTests<State, Action>(
                generateState = {
                    when (it) {
                        State.Error::class -> errorState
                        State.Content::class -> contentState
                        State.WaitingForAuthState::class -> waitingForAuthState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.Load::class -> loadAction
                        Action.LoadPaymentOptionListFailed::class -> failAction
                        Action.LoadPaymentOptionListSuccess::class -> successDataAction
                        Action.ProceedWithPaymentMethod::class -> proceedWithPaymentMethodAction
                        Action.Logout::class -> logoutAction
                        Action.PaymentAuthSuccess::class -> successAuth
                        Action.PaymentAuthCancel::class -> cancelAuth
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    if (state == waitingForAuthState && action !in listOf(successAuth, cancelAuth, failAction)) {
                        waitingForAuthState.content
                    } else {
                        when (state to action) {
                            contentState to loadAction -> State.Loading
                            contentState to proceedWithPaymentMethodAction -> contentState
                            contentState to logoutAction -> State.Loading

                            errorState to loadAction -> State.Loading

                            loadingState to successDataAction -> contentState
                            loadingState to failAction -> errorState

                            waitingForAuthState to cancelAuth -> contentState
                            waitingForAuthState to successAuth -> waitingForAuthState
                            waitingForAuthState to failAction -> errorState
                            else -> state
                        }
                    }
                }
            )
        }
    }

    private val logic =
        PaymentOptionsListBusinessLogic(
            mock(), mock(), mock(), mock(), PaymentParameters(
                amount = Amount(BigDecimal.ONE, RUB),
                title = "",
                subtitle = "",
                clientApplicationKey = "",
                shopId = "",
                savePaymentMethod = SavePaymentMethod.ON,
                authCenterClientId = ""
            ), mock()
        )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}