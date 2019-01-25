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

package ru.yandex.money.android.sdk.paymentAuth

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.model.AuthType
import ru.yandex.money.android.sdk.model.AuthTypeState
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import java.math.BigDecimal

internal class RequestPaymentAuthUseCaseTest {

    @Mock
    private lateinit var userGateway: CurrentUserGateway
    @Mock
    private lateinit var authTypeGateway: PaymentAuthTypeGateway

    private lateinit var useCase: RequestPaymentAuthUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = RequestPaymentAuthUseCase(authTypeGateway)
    }

    @Test
    fun shouldInvokeGateway_Then_ReturnResult() {
        // prepare
        val linkWalletToApp = false
        val amount = Amount(BigDecimal.TEN, RUB)
        `when`(authTypeGateway.getPaymentAuthType(linkWalletToApp, amount)).thenReturn(
            AuthTypeState(
                AuthType.SMS,
                1
            )
        )

        // invoke
        useCase(RequestPaymentAuthInputModel(linkWalletToApp, amount))

        // assert
        verify(authTypeGateway).getPaymentAuthType(linkWalletToApp, amount)
        verifyNoMoreInteractions(userGateway, authTypeGateway)
    }
}