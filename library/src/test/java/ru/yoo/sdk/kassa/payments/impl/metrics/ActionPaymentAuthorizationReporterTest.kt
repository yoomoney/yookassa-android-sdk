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

import android.graphics.drawable.Drawable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.impl.contract.ContractSuccessViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.SavePaymentMethodViewModel
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentOptionViewModel
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthSuccessOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthWrongAnswerOutputModel

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionPaymentAuthorizationReporterTest {

    @Mock
    private lateinit var presenter: Presenter<ProcessPaymentAuthOutputModel, ContractSuccessViewModel>
    @Mock
    private lateinit var reporter: Reporter

    private lateinit var actionReporter: ActionPaymentAuthorizationReporter

    @Before
    fun setUp() {
        on(presenter(any() ?: ProcessPaymentAuthSuccessOutputModel())).thenReturn(
            ContractSuccessViewModel(
                "",
                "",
                PaymentOptionViewModel(0, mock(Drawable::class.java), "", ""),
                "", false, SavePaymentMethodViewModel.UserSelects, false, null, false
            )
        )
        actionReporter = ActionPaymentAuthorizationReporter(presenter, reporter)
    }

    @Test
    fun `should report AuthPaymentStatusSuccess when ProcessPaymentAuthSuccessOutputModel`() {
        // prepare
        val outputModel =
            ProcessPaymentAuthSuccessOutputModel()

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report("actionPaymentAuthorization", listOf(AuthPaymentStatusSuccess()))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report AuthPaymentStatusFail when ProcessPaymentAuthWrongAnswerOutputModel`() {
        // prepare
        val outputModel =
            ProcessPaymentAuthWrongAnswerOutputModel("password")

        // invoke
        actionReporter(outputModel)

        // assert
        inOrder(presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(reporter).report("actionPaymentAuthorization", listOf(AuthPaymentStatusFail()))
            verifyNoMoreInteractions()
        }
    }
}
