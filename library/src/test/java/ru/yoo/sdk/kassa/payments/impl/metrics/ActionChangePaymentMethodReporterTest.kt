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
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.PaymentOptionListSuccessViewModel
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.changeOption.ChangePaymentOptionOutputModel

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionChangePaymentMethodReporterTest {

    @Mock
    private lateinit var presenter: Presenter<ChangePaymentOptionOutputModel, PaymentOptionListSuccessViewModel>
    @Mock
    private lateinit var reporter: Reporter

    private lateinit var actionChangePaymentMethodReporter: ActionChangePaymentMethodReporter

    @Before
    fun setUp() {
        on(presenter.invoke(listOf()))
            .thenReturn(PaymentOptionListSuccessViewModel(listOf(), false))
        actionChangePaymentMethodReporter = ActionChangePaymentMethodReporter(presenter, reporter)
    }

    @Test
    fun `should report actionChangePaymentMethod when invoke`() {
        // prepare
        val outputModel = listOf<PaymentOption>()

        // invoke
        actionChangePaymentMethodReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report("actionChangePaymentMethod", null)
            verifyNoMoreInteractions()
        }
    }
}
