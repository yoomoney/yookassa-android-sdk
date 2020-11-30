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

package ru.yoo.sdk.kassa.payments.impl.metrics

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.impl.extensions.RUB
import ru.yoo.sdk.kassa.payments.impl.paymentOptionInfo.PaymentOptionInfoLinkedCardViewModel
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class RecurringCardScreenOpenedReporterTest {

    private val stubOutputModel = TokenizePaymentOptionInfoRequired(
        PaymentIdCscConfirmation(
            id = 0,
            charge = Amount(BigDecimal.TEN, RUB),
            paymentMethodId = "123",
            fee = null,
            first = "123456",
            last = "7890",
            expiryYear = "2020",
            expiryMonth = "12",
            savePaymentMethodAllowed = true
        ),
        savePaymentMethod = true
    )
    private val testViewModel = PaymentOptionInfoLinkedCardViewModel(0, true, "123456****7890", 123)

    @Mock
    private lateinit var presenter: Presenter<TokenizeOutputModel, Any>
    @Mock
    private lateinit var mockReporter: Reporter

    @Before
    fun setUp() {
        on(presenter(stubOutputModel)).thenReturn(testViewModel)
    }

    @Test
    fun shouldSendNameAndAuthType() {
        // prepare
        val reporter = RecurringCardScreenOpenedReporter(presenter, mockReporter)

        // invoke
        reporter(stubOutputModel)

        // assert
        Mockito.verify(mockReporter).report("screenRecurringCardForm", null)
    }
}