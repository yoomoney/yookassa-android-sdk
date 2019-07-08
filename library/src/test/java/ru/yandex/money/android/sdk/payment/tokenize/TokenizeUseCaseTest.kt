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

package ru.yandex.money.android.sdk.payment.tokenize

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.inOrder
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.createGooglePayPaymentOptionWithFee
import ru.yandex.money.android.sdk.createLinkedCardPaymentOption
import ru.yandex.money.android.sdk.createNewCardPaymentOption
import ru.yandex.money.android.sdk.createSbolSmsInvoicingPaymentOption
import ru.yandex.money.android.sdk.createWalletPaymentOption
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.model.AbstractWallet
import ru.yandex.money.android.sdk.model.Confirmation
import ru.yandex.money.android.sdk.model.NoConfirmation
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.SelectedOptionNotFoundException
import ru.yandex.money.android.sdk.model.WalletInfo
import ru.yandex.money.android.sdk.payment.CheckPaymentAuthRequiredGateway
import ru.yandex.money.android.sdk.payment.GetLoadedPaymentOptionListGateway
import java.math.BigDecimal

internal class TokenizeUseCaseTest {

    private val getLoadedPaymentOptionListGateway: GetLoadedPaymentOptionListGateway = mock()
    private val tokenizeGateway: TokenizeGateway = mock()
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway = mock()
    private val convertToConfirmation: (PaymentOption) -> Confirmation = mock {
        on { invoke(any()) } doReturn NoConfirmation
    }
    private var useCase: TokenizeUseCase = TokenizeUseCase(
        getLoadedPaymentOptionListGateway = getLoadedPaymentOptionListGateway,
        tokenizeGateway = tokenizeGateway,
        checkPaymentAuthRequiredGateway = checkPaymentAuthRequiredGateway,
        convertToConfirmation = convertToConfirmation
    )

    @Test(expected = SelectedOptionNotFoundException::class)
    fun shouldThrowOptionNotFoundException() {
        // prepare
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(emptyList())

        // invoke
        useCase(TokenizeInputModel(1, true))

        // assert that SelectedOptionNotFoundException thrown
    }

    @Test
    fun shouldRequestToken() {
        // prepare
        val testPaymentOption = createWalletPaymentOption(1)
        val testPaymentOptionInfo = WalletInfo()
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testPaymentOption))
        whenever(
            tokenizeGateway.getToken(
                testPaymentOption,
                testPaymentOptionInfo,
                true,
                NoConfirmation
            )
        ).thenReturn("123")

        // invoke
        useCase(TokenizeInputModel(1, true))

        // assert
        inOrder(getLoadedPaymentOptionListGateway, tokenizeGateway, checkPaymentAuthRequiredGateway).apply {
            verify(getLoadedPaymentOptionListGateway).getLoadedPaymentOptions()
            verify(tokenizeGateway).getToken(testPaymentOption, testPaymentOptionInfo, true,
                NoConfirmation
            )
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldRequestAuthorization() {
        // prepare
        val testPaymentOption = createWalletPaymentOption(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testPaymentOption))
        whenever(checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(true)

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, true))

        assertThat(outputModel, instanceOf(TokenizePaymentAuthRequiredOutputModel::class.java))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrow_IllegalStateException_When_TryToTokenizeAbstractWallet() {
        // prepare
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(
            listOf(AbstractWallet(1, Amount(BigDecimal.TEN, RUB), null))
        )

        // invoke
        useCase(TokenizeInputModel(1, false))

        // assert that CannotTokenizeAbstractWalletException thrown
    }

    @Test
    fun `should return TokenizePaymentOptionInfoRequired with LinkedCard when LinkedCardOptionInfo not present`() {
        // prepare
        val paymentOption = createLinkedCardPaymentOption(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(paymentOption))

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, false)) as TokenizePaymentOptionInfoRequired

        // assert
        assertThat(outputModel.option, equalTo(paymentOption))
    }

    @Test
    fun `should return TokenizePaymentOptionInfoRequired with NewCard when BankCardOptionInfo not present`() {
        // prepare
        val paymentOption = createNewCardPaymentOption(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(paymentOption))

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, false)) as TokenizePaymentOptionInfoRequired

        // assert
        assertThat(outputModel.option, equalTo(paymentOption))
    }

    @Test
    fun `should return TokenizePaymentOptionInfoRequired with SbolSmsInvoicing when SbolSmsInvoicingInfo not present`() {
        // prepare
        val paymentOption = createSbolSmsInvoicingPaymentOption(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(paymentOption))

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, false)) as TokenizePaymentOptionInfoRequired

        // assert
        assertThat(outputModel.option, equalTo(paymentOption))
    }

    @Test
    fun `should return TokenizePaymentOptionInfoRequired with GooglePay when GooglePayOptionInfo not present`() {
        // prepare
        val paymentOption = createGooglePayPaymentOptionWithFee(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(paymentOption))

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, false)) as TokenizePaymentOptionInfoRequired

        // assert
        assertThat(outputModel.option, equalTo(paymentOption))
    }

    @Test
    fun shouldReturn_PaymentOption() {
        // prepare
        val paymentOption = createWalletPaymentOption(1)
        whenever(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(paymentOption))
        whenever(tokenizeGateway.getToken(any(), any(), any(), any())).thenReturn("test token")

        // invoke
        val outputModel = useCase(TokenizeInputModel(1, false)) as TokenOutputModel

        // assert
        assertThat(outputModel.option, equalTo(paymentOption))
    }
}
