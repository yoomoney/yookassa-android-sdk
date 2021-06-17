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

package ru.yoomoney.sdk.kassa.payments.impl.userAuth

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginSchemeAuthSdk
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuth
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthBusinessLogic
import ru.yoomoney.sdk.march.generateBusinessLogicTests
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
            val auxToken = "auxToken"
            val cryptogram = "cryptogram"

            val waitingForAuthStartedState = MoneyAuth.State.WaitingForAuthStarted
            val authorizeState = MoneyAuth.State.Authorize(MoneyAuth.AuthorizeStrategy.InApp)
            val completeState = MoneyAuth.State.CompleteAuth
            val cancelAuth = MoneyAuth.State.CancelAuth


            val requireAuthAction = MoneyAuth.Action.RequireAuth(false)
            val authorizedAction = MoneyAuth.Action.Authorized(
                "any", null, "any", MoneyAuthLoginSchemeAuthSdk()
            )
            val authCancelledAction = MoneyAuth.Action.AuthCancelled
            val getTransferData = MoneyAuth.Action.GetTransferData(cryptogram)

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
                        MoneyAuth.Action.GetTransferData::class -> getTransferData
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
        showState = mock(),
        source = mock(),
        tmxSessionIdStorage = TmxSessionIdStorage(),
        currentUserRepository = mock(),
        userAuthInfoRepository = mock(),
        paymentOptionsListUseCase = mock(),
        getTransferDataUseCase = mock(),
        paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, RUB),
            title = "shopTitle",
            subtitle = "shopSubtitle",
            clientApplicationKey = "clientApplicationKey",
            shopId = "shopId",
            savePaymentMethod = SavePaymentMethod.ON,
            authCenterClientId = "authCenterClientId"
        ),
        loadedPaymentOptionListRepository = mock()
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertThat(actual.state, CoreMatchers.equalTo(expected))
    }
}