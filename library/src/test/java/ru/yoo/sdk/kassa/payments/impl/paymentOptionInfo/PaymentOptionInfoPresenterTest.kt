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

package ru.yoo.sdk.kassa.payments.impl.paymentOptionInfo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.impl.extensions.RUB
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.model.ViewModel
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentAuthRequiredOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class PaymentOptionInfoPresenterTest {

    @Mock
    private lateinit var otherPresenter: Presenter<TokenizeOutputModel, Any>
    private lateinit var presenter: PaymentOptionInfoPresenter

    @Before
    fun setUp() {
        val stub = TokenOutputModel("token", createNewCardPaymentOption(0))
        on(otherPresenter.invoke(any() ?: stub)).thenReturn(ViewModel())
        presenter = PaymentOptionInfoPresenter(otherPresenter)
    }

    @Test
    fun `should invoke wrapped presenter if TokenOutputModel`() {
        // prepare
        val outputMode = TokenOutputModel("token",
            createNewCardPaymentOption(0)
        )

        // invoke
        presenter(outputMode)

        // assert
        verify(otherPresenter).invoke(outputMode)
    }

    @Test
    fun `should invoke wrapped presenter if TokenizePaymentAuthRequiredOutputModel`() {
        // prepare
        val outputMode = TokenizePaymentAuthRequiredOutputModel(
            Amount(
                BigDecimal.TEN,
                RUB
            )
        )

        // invoke
        presenter(outputMode)

        // assert
        verify(otherPresenter).invoke(outputMode)
    }

    @Test
    fun `should return PaymentOptionInfoBankCardViewModel if TokenizePaymentOptionInfoRequired with NewCard`() {
        // prepare
        val outputMode = TokenizePaymentOptionInfoRequired(
            createNewCardPaymentOption(
                0
            ), true)

        // invoke
        presenter(outputMode) as PaymentOptionInfoBankCardViewModel

        // assert
        verifyZeroInteractions(otherPresenter)
    }

    @Test
    fun `should return PaymentOptionInfoLinkedCardViewModel if TokenizePaymentOptionInfoRequired with LinkedCard`() {
        // prepare
        val option = createLinkedCardPaymentOption(0) as LinkedCard
        val outputMode = TokenizePaymentOptionInfoRequired(option, true)

        // invoke
        val viewModel = presenter(outputMode) as PaymentOptionInfoLinkedCardViewModel

        // assert
        assertThat(viewModel.pan, equalTo(option.pan.chunked(4).joinToString(" ") as CharSequence))
        verifyZeroInteractions(otherPresenter)
    }
}
