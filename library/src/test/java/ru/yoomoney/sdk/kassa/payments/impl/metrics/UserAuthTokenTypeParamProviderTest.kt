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

package ru.yoomoney.sdk.kassa.payments.impl.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTokenType
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTokenTypeMultiple
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTokenTypeSingle
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTokenTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.on
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class UserAuthTokenTypeParamProviderTest {

    @Mock
    private lateinit var paymentAuthTokenRepository: PaymentAuthTokenRepository
    private lateinit var userAuthTokenTypeParamProvider: UserAuthTokenTypeParamProvider

    @Before
    fun setUp() {
        userAuthTokenTypeParamProvider =
            UserAuthTokenTypeParamProvider(
                paymentAuthTokenRepository
            )
    }

    @Test
    fun `should return AuthTokenTypeSingle if paymentAuthToken not persisted`() {
        // prepare
        on(paymentAuthTokenRepository.isPaymentAuthPersisted).thenReturn(false)

        // invoke
        val tokenTypeParam = userAuthTokenTypeParamProvider()

        // assert
        assertThat(tokenTypeParam, equalTo(AuthTokenTypeSingle() as AuthTokenType))
    }

    @Test
    fun `should return AuthTokenTypeMultiple if paymentAuthToken persisted`() {
        // prepare
        on(paymentAuthTokenRepository.isPaymentAuthPersisted).thenReturn(true)

        // invoke
        val tokenTypeParam = userAuthTokenTypeParamProvider()

        // assert
        assertThat(tokenTypeParam, equalTo(AuthTokenTypeMultiple() as AuthTokenType))
    }
}