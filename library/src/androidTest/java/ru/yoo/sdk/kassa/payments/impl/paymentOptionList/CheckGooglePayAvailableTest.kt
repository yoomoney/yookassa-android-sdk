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

package ru.yoo.sdk.kassa.payments.impl.paymentOptionList

import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.GooglePayParameters
import ru.yoo.sdk.kassa.payments.impl.extensions.RUB
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListGateway
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

internal class CheckGooglePayAvailableGatewayTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    @Test
    fun test() {
        // prepare
        val shopId = "stub shopId"
        val gateway =
            GooglePayIntegration(
                context = InstrumentationRegistry.getInstrumentation().targetContext,
                shopId = shopId,
                useTestEnvironment = true,
                loadedPaymentOptionsGateway = object : GetLoadedPaymentOptionListGateway {
                    override fun getLoadedPaymentOptions(): List<PaymentOption> {
                        return listOf(
                            NewCard(
                                1,
                                Amount(BigDecimal.TEN, RUB), null, false
                            )
                        )
                    }
                },
                googlePayParameters = GooglePayParameters()
            )

        // invoke
        val available = gateway.checkGooglePayAvailable()

        // assert
        assertThat("may be available", available)
    }
}
