/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.payment.loadOptionList

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.AnonymousUser
import ru.yandex.money.android.sdk.createGooglePayPaymentOption
import ru.yandex.money.android.sdk.createNewCardPaymentOption
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.on
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class PaymentOptionListWithGooglePayFilterGatewayTest {

    private val testAmount = Amount(BigDecimal.TEN, RUB)
    private val testCurrentUser = AnonymousUser
    private val testPaymentOptions = listOf(
        createNewCardPaymentOption(0),
        createGooglePayPaymentOption(1)
    )

    @Mock
    private lateinit var paymentOptionListGateway: PaymentOptionListGateway
    @Mock
    private lateinit var checkGooglePayAvailableGateway: CheckGooglePayAvailableGateway

    private lateinit var gateway: PaymentOptionListWithGooglePayFilterGateway

    @Before
    fun setUp() {
        on(paymentOptionListGateway.getPaymentOptions(testAmount, testCurrentUser)).thenReturn(
            testPaymentOptions
        )
        gateway = PaymentOptionListWithGooglePayFilterGateway(paymentOptionListGateway, checkGooglePayAvailableGateway)
    }

    @Test
    fun `should return payment option list with GooglePay if GooglaPay available`() {
        // prepare
        on(checkGooglePayAvailableGateway.checkGooglePayAvailable()).thenReturn(true)

        // invoke
        val paymentOptions = gateway.getPaymentOptions(testAmount, testCurrentUser)

        // assert
        assertThat(paymentOptions, hasItems(*testPaymentOptions.toTypedArray()))
    }

    @Test
    fun `should return payment option list without GooglePay if google pay not available`() {
        // prepare
        on(checkGooglePayAvailableGateway.checkGooglePayAvailable()).thenReturn(false)

        // invoke
        val paymentOptions = gateway.getPaymentOptions(testAmount, testCurrentUser)

        // assert
        assertThat(paymentOptions, hasItems(testPaymentOptions[0]))
    }

    @Test
    fun `should call CheckGooglePayAvailableGateway once per object`() {
        // prepare
        on(checkGooglePayAvailableGateway.checkGooglePayAvailable()).thenReturn(true)

        // invoke
        gateway.getPaymentOptions(testAmount, testCurrentUser)
        gateway.getPaymentOptions(testAmount, testCurrentUser)

        // assert
        verify(checkGooglePayAvailableGateway).checkGooglePayAvailable()
    }
}