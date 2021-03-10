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

package ru.yoo.sdk.kassa.payments.impl.userAuth

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoo.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoo.sdk.kassa.payments.extensions.RUB
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuth
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuthBusinessLogic
import ru.yoo.sdk.march.generateBusinessLogicTests
import java.math.BigDecimal

@RunWith(Parameterized::class)
internal class MoneyAuthBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: MoneyAuth.State,
    val action: MoneyAuth.Action,
    val expected: MoneyAuth.State
) {
    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val waitingForAuthStartedState = MoneyAuth.State.WaitingForAuthStarted
            val authorizeState = MoneyAuth.State.Authorize("any")
            val completeState = MoneyAuth.State.CompleteAuth
            val cancelAuth = MoneyAuth.State.CancelAuth


            val requireAuthAction = MoneyAuth.Action.RequireAuth
            val authorizedAction = MoneyAuth.Action.Authorized(
                "any", null, "any"
            )

            val authCancelledAction = MoneyAuth.Action.AuthCancelled

            return generateBusinessLogicTests<MoneyAuth.State, MoneyAuth.Action>(
                generateState = {
                    when (it) {
                        MoneyAuth.State.WaitingForAuthStarted::class -> waitingForAuthStartedState
                        MoneyAuth.State.Authorize::class -> authorizeState
                        MoneyAuth.State.CompleteAuth::class -> completeState
                        MoneyAuth.State.CancelAuth::class -> cancelAuth
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        MoneyAuth.Action.RequireAuth::class -> requireAuthAction
                        MoneyAuth.Action.Authorized::class -> authorizedAction
                        MoneyAuth.Action.AuthCancelled::class -> authCancelledAction
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        authorizeState to requireAuthAction -> authorizeState
                        authorizeState to authorizedAction -> completeState
                        authorizeState to authCancelledAction -> waitingForAuthStartedState
                        completeState to requireAuthAction -> authorizeState
                        waitingForAuthStartedState to requireAuthAction -> authorizeState
                        else -> {
                            if (state == cancelAuth) {
                                waitingForAuthStartedState
                            } else {
                                state
                            }
                        }
                    }
                }
            )
        }
    }

    private val logic = MoneyAuthBusinessLogic(
        mock(), mock(), "any",
        TmxSessionIdStorage(), mock(), mock(), mock(),
        PaymentParameters(
            amount = Amount(BigDecimal.TEN, RUB),
            title = "shopTitle",
            subtitle = "shopSubtitle",
            clientApplicationKey = "clientApplicationKey",
            shopId = "shopId",
            savePaymentMethod = SavePaymentMethod.ON,
            authCenterClientId = "authCenterClientId"
        ),
        mock()
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertThat(actual.state, CoreMatchers.equalTo(expected))
    }
}