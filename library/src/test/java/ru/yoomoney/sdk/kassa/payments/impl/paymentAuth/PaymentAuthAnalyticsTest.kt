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

package ru.yoomoney.sdk.kassa.payments.impl.paymentAuth

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.metrics.AuthPaymentStatusFail
import ru.yoomoney.sdk.kassa.payments.metrics.AuthPaymentStatusSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthAnalytics

class PaymentAuthAnalyticsTest {
    private val reporter: Reporter = mock()

    private val paymentAuthAnalytics = PaymentAuthAnalytics(
        reporter = reporter,
        businessLogic = mock()
    )

    @Test
    fun `verify money payment auth success analytics sends`() {
        // given

        // when
        paymentAuthAnalytics(
            PaymentAuth.State.Loading,
            PaymentAuth.Action.ProcessAuthSuccess
        )

        // then
        verify(reporter).report("actionPaymentAuthorization", listOf(AuthPaymentStatusSuccess()))
    }

    @Test
    fun `verify money payment auth fail wrong answer analytics sends`() {
        // given

        // when
        paymentAuthAnalytics(
            PaymentAuth.State.Loading,
            PaymentAuth.Action.ProcessAuthWrongAnswer(AuthTypeState.SMS(1, 1, null, null))
        )

        // then
        verify(reporter).report("actionPaymentAuthorization", listOf(AuthPaymentStatusFail()))
    }

    @Test
    fun `verify money payment auth fail analytics sends`() {
        // given

        // when
        paymentAuthAnalytics(
            PaymentAuth.State.Loading,
            PaymentAuth.Action.ProcessAuthFailed(NotImplementedError())
        )

        // then
        verify(reporter).report("actionPaymentAuthorization", listOf(AuthPaymentStatusFail()))
    }

}