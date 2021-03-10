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

package ru.yoo.sdk.kassa.payments.paymentOptionList

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoo.sdk.kassa.payments.extensions.RUB
import ru.yoo.sdk.march.Effect
import java.math.BigDecimal

internal class PaymentOptionsListBusinessLogicEffectTest {

    private val showState: (PaymentOptionList.State) -> PaymentOptionList.Action = mock()
    private val showEffect: (PaymentOptionList.Effect) -> Unit = mock()
    private val source: () -> PaymentOptionList.Action = mock()
    private val useCase: PaymentOptionsListUseCase = mock()
    private val contentState = PaymentOptionList.State.Content(PaymentOptionListSuccessOutputModel(listOf()))

    private val logic =
        PaymentOptionsListBusinessLogic(
            showState = { showState(it) },
            showEffect = { showEffect(it) },
            source = { source() },
            useCase = useCase,
            paymentParameters = PaymentParameters(
                amount = Amount(BigDecimal.ONE, RUB),
                title = "",
                subtitle = "",
                clientApplicationKey = "",
                shopId = "",
                savePaymentMethod = SavePaymentMethod.ON,
                authCenterClientId = ""
            ),
            logoutUseCase = mock()
        )

    @Test
    fun `Should send ProceedWithPaymentMethod effect with Content state and ProceedWithPaymentMethod action`() {
        // given
        val paymentOptionId = 11
        val expected = PaymentOptionList.Effect.ProceedWithPaymentMethod
        val out = logic(
            contentState,
            PaymentOptionList.Action.ProceedWithPaymentMethod(paymentOptionId)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
        verify(source).invoke()
    }

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