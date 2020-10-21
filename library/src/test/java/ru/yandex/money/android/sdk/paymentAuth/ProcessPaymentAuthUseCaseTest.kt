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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations
import ru.yandex.money.android.sdk.model.AnonymousUser
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.CurrentUserGateway

internal class ProcessPaymentAuthUseCaseTest {

    private val testPassphrase = "test passphrase"
    private val paymentAuthToken = PaymentAuthToken("test token")
    private val testUser = AuthorizedUser()

    @Mock
    private lateinit var currentUserGateway: CurrentUserGateway
    @Mock
    private lateinit var processPaymentAuthGateway: ProcessPaymentAuthGateway
    @Mock
    private lateinit var paymentAuthTokenGateway: PaymentAuthTokenGateway

    private lateinit var useCase: ProcessPaymentAuthUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = ProcessPaymentAuthUseCase(
            processPaymentAuthGateway = processPaymentAuthGateway,
            currentUserGateway = currentUserGateway,
            paymentAuthTokenGateway = paymentAuthTokenGateway
        )
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrow_IllegalStateException_When_TryToProcessWithAnonymousUser() {
        // prepare
        on(currentUserGateway.currentUser).thenReturn(AnonymousUser)

        // invoke
        useCase(RequiredProcessPaymentAuthInputModel(testPassphrase, false))

        // assert that IllegalStateException thrown
    }

    @Test
    fun `should not persist payment auth token when started without save flag`() {
        // prepare
        on(currentUserGateway.currentUser).thenReturn(testUser)
        on(processPaymentAuthGateway.getPaymentAuthToken(testUser, testPassphrase)).thenReturn(paymentAuthToken)

        // invoke
        val outputModel = useCase(RequiredProcessPaymentAuthInputModel(testPassphrase, false))

        // assert
        assertThat(outputModel, instanceOf(ProcessPaymentAuthSuccessOutputModel::class.java))
        verify(currentUserGateway).currentUser
        verify(processPaymentAuthGateway).getPaymentAuthToken(testUser, testPassphrase)
        verify(paymentAuthTokenGateway).paymentAuthToken = "test token"
        verifyNoMoreInteractions(currentUserGateway, processPaymentAuthGateway, paymentAuthTokenGateway)
    }

    @Test
    fun `should persist payment auth token when started with save flag`() {
        // prepare
        on(currentUserGateway.currentUser).thenReturn(testUser)
        on(processPaymentAuthGateway.getPaymentAuthToken(testUser, testPassphrase)).thenReturn(paymentAuthToken)

        // invoke
        val outputModel = useCase(RequiredProcessPaymentAuthInputModel(testPassphrase, true))

        // assert
        assertThat(outputModel, instanceOf(ProcessPaymentAuthSuccessOutputModel::class.java))
        verify(currentUserGateway).currentUser
        verify(processPaymentAuthGateway).getPaymentAuthToken(testUser, testPassphrase)
        verify(paymentAuthTokenGateway).paymentAuthToken = "test token"
        verify(paymentAuthTokenGateway).persistPaymentAuth()
        verifyNoMoreInteractions(currentUserGateway, processPaymentAuthGateway, paymentAuthTokenGateway)
    }

    @Test
    fun shouldReturn_ProcessPaymentWrongAnswer_When_GatewayResponse_Is_WrongAnswer() {
        // prepare
        on(currentUserGateway.currentUser).thenReturn(testUser)
        on(processPaymentAuthGateway.getPaymentAuthToken(testUser, testPassphrase)).thenReturn(PaymentAuthWrongAnswer())

        // invoke
        val outputModel = useCase(RequiredProcessPaymentAuthInputModel(testPassphrase, true))

        // assert
        assertThat(outputModel, instanceOf(ProcessPaymentAuthWrongAnswerOutputModel::class.java))
        verify(currentUserGateway).currentUser
        verify(processPaymentAuthGateway).getPaymentAuthToken(testUser, testPassphrase)
        verifyNoMoreInteractions(currentUserGateway, processPaymentAuthGateway, paymentAuthTokenGateway)
    }
}
