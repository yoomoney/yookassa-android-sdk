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

package ru.yandex.money.android.sdk.payment.selectOption

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.SelectedOptionNotFoundException
import ru.yandex.money.android.sdk.createAbstractWalletPaymentOption
import ru.yandex.money.android.sdk.createLinkedCardPaymentOption
import ru.yandex.money.android.sdk.createNewCardPaymentOption
import ru.yandex.money.android.sdk.createWalletPaymentOption
import ru.yandex.money.android.sdk.payment.CheckPaymentAuthRequiredGateway
import ru.yandex.money.android.sdk.payment.GetLoadedPaymentOptionListGateway

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class SelectPaymentOptionUseCaseTest {

    private val testNewCardPaymentOption: PaymentOption = createNewCardPaymentOption(0)
    private val testNewCardPaymentOption1: PaymentOption = createNewCardPaymentOption(1)
    private val testWalletPaymentOption = createWalletPaymentOption(0)
    private val testLinkedCardPaymentOption = createLinkedCardPaymentOption(0)
    private val testAbstractWalletPaymentOption = createAbstractWalletPaymentOption(0)

    @Mock
    private lateinit var getLoadedPaymentOptionListGateway: GetLoadedPaymentOptionListGateway
    @Mock
    private lateinit var checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway

    private lateinit var useCase: SelectPaymentOptionUseCase

    @Before
    fun setUp() {
        useCase = SelectPaymentOptionUseCase(getLoadedPaymentOptionListGateway, checkPaymentAuthRequiredGateway)
    }

    @Test(expected = SelectedOptionNotFoundException::class)
    fun shouldThrowExceptionIfPaymentOptionNotFound() {
        // prepare

        // invoke
        useCase(0)

        // assert that fail with exception SelectedOptionNotFoundException
    }

    @Test
    fun shouldReturnPaymentOptionAndNoAnotherOptionsFlag() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testNewCardPaymentOption))

        // invoke
        val model = useCase(testNewCardPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testNewCardPaymentOption))
        assertThat("no another option", !model.hasAnotherOptions)
        assertThat("wallet linking impossible", !model.walletLinkingPossible)
    }

    @Test
    fun shouldReturnPaymentOptionAndHasAnotherOptionsFlag() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions())
                .thenReturn(listOf(testNewCardPaymentOption, testNewCardPaymentOption1))

        // invoke
        val model = useCase(testNewCardPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testNewCardPaymentOption))
        assertThat("has another option", model.hasAnotherOptions)
        assertThat("wallet linking impossible", !model.walletLinkingPossible)
    }

    @Test
    fun shouldReturn_LinkWalletToAppAllowed_When_WalletSelected_And_PaymentAuthRequired() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testWalletPaymentOption))
        `when`(checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(true)

        // invoke
        val model = useCase(testWalletPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testWalletPaymentOption))
        assertThat("no another option", !model.hasAnotherOptions)
        assertThat("wallet linking possible", model.walletLinkingPossible)
    }

    @Test
    fun shouldReturn_LinkWalletToAppAllowed_When_LinkedCardSelected_And_PaymentAuthRequired() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testLinkedCardPaymentOption))
        `when`(checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(true)

        // invoke
        val model = useCase(testLinkedCardPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testLinkedCardPaymentOption))
        assertThat("no another option", !model.hasAnotherOptions)
        assertThat("wallet linking possible", model.walletLinkingPossible)
    }

    @Test
    fun shouldReturn_LinkWalletToAppDenied_When_WalletSelected_And_PaymentAuthNotRequired() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testWalletPaymentOption))
        `when`(checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(false)

        // invoke
        val model = useCase(testWalletPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testWalletPaymentOption))
        assertThat("no another option", !model.hasAnotherOptions)
        assertThat("wallet linking impossible", !model.walletLinkingPossible)
    }

    @Test
    fun shouldReturn_LinkWalletToAppDenied_When_LinkedCardSelected_And_PaymentAuthNotRequired() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testLinkedCardPaymentOption))
        `when`(checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(false)

        // invoke
        val model = useCase(testLinkedCardPaymentOption.id) as SelectedPaymentOptionOutputModel

        // assert
        assertThat(model.paymentOption, equalTo(testLinkedCardPaymentOption))
        assertThat("no another option", !model.hasAnotherOptions)
        assertThat("wallet linking impossible", !model.walletLinkingPossible)
    }

    @Test
    fun shouldReturn_UserAuthRequired_If_AbstractWalletSelected() {
        // prepare
        `when`(getLoadedPaymentOptionListGateway.getLoadedPaymentOptions()).thenReturn(listOf(testAbstractWalletPaymentOption))

        // invoke
        val model = useCase(testAbstractWalletPaymentOption.id)

        // assert
        assertThat(model, instanceOf(UserAuthRequired::class.java))
    }
}