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

package ru.yoomoney.sdk.kassa.payments.utils

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class WebViewActivityRobolectricTest {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception if not https url present`() {
        // prepare
        Checkout.createTokenizeIntent(
            RuntimeEnvironment.application,
            PaymentParameters(
                amount = Amount(BigDecimal.ONE, RUB),
                title = "",
                subtitle = "",
                clientApplicationKey = "",
                shopId = "",
                savePaymentMethod = SavePaymentMethod.ON,
                authCenterClientId = ""
            )
        )
        val url = "http://wrong.scheme.url/"

        // invoke
        Checkout.createConfirmationIntent(
            context = RuntimeEnvironment.application,
            confirmationUrl = url,
            paymentMethodType = PaymentMethodType.BANK_CARD
        )

        // assert that exception thrown
    }
}
