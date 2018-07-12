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

package ru.yandex.money.android.sdk.impl.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTokenGateway

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class UserAuthTokenTypeParamProviderTest {

    @Mock
    private lateinit var paymentAuthTokenGateway: PaymentAuthTokenGateway
    private lateinit var userAuthTokenTypeParamProvider: UserAuthTokenTypeParamProvider

    @Before
    fun setUp() {
        userAuthTokenTypeParamProvider = UserAuthTokenTypeParamProvider(paymentAuthTokenGateway)
    }

    @Test
    fun `should return AuthTokenTypeSingle if paymentAuthToken not persisted`() {
        // prepare
        on(paymentAuthTokenGateway.isPaymentAuthPersisted).thenReturn(false)

        // invoke
        val tokenTypeParam = userAuthTokenTypeParamProvider()

        // assert
        assertThat(tokenTypeParam, equalTo(AuthTokenTypeSingle() as AuthTokenType))
    }

    @Test
    fun `should return AuthTokenTypeMultiple if paymentAuthToken persisted`() {
        // prepare
        on(paymentAuthTokenGateway.isPaymentAuthPersisted).thenReturn(true)

        // invoke
        val tokenTypeParam = userAuthTokenTypeParamProvider()

        // assert
        assertThat(tokenTypeParam, equalTo(AuthTokenTypeMultiple() as AuthTokenType))
    }
}