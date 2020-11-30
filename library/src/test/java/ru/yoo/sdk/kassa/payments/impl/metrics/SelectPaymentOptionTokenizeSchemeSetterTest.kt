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

package ru.yoo.sdk.kassa.payments.impl.metrics

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.MockitoAnnotations
import ru.yoo.sdk.kassa.payments.createAbstractWalletPaymentOption
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.UseCase
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionInputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel

private typealias SelectPaymentOptionUseCase = UseCase<SelectPaymentOptionInputModel, SelectedPaymentOptionOutputModel>

@RunWith(Parameterized::class)
internal class SelectPaymentOptionTokenizeSchemeSetterTest(
    private val name: String,
    private val paymentOption: PaymentOption,
    private val tokenizeScheme: TokenizeScheme
) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "should set TokenizeSchemeWallet when Wallet selected",
                    createWalletPaymentOption(0),
                    TokenizeSchemeWallet()
                ),
                arrayOf(
                    "should set TokenizeSchemeWallet when AbstractWallet selected",
                    createAbstractWalletPaymentOption(0),
                    TokenizeSchemeWallet()
                ),
                arrayOf(
                    "should set TokenizeSchemeLinkedCard when LinkedCard selected",
                    createLinkedCardPaymentOption(0),
                    TokenizeSchemeLinkedCard()
                ),
                arrayOf(
                    "should set TokenizeSchemeBankCard when BankCard selected",
                    createNewCardPaymentOption(0),
                    TokenizeSchemeBankCard()
                ),
                arrayOf(
                    "should set TokenizeSchemeSbolSms when SbolSmsInvoicing selected",
                    createSbolSmsInvoicingPaymentOption(0),
                    TokenizeSchemeSbolSms()
                )
            )
        }
    }

    @Mock
    private lateinit var selectPaymentOptionUseCase: SelectPaymentOptionUseCase
    @Mock
    private lateinit var tokenizeSchemeConsumer: TokenizeSchemeConsumer
    private lateinit var selectPaymentOptionTokenizeSchemeSetter: SelectPaymentOptionTokenizeSchemeSetter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        selectPaymentOptionTokenizeSchemeSetter = SelectPaymentOptionTokenizeSchemeSetter(
            useCase = selectPaymentOptionUseCase,
            setTokenizeScheme = tokenizeSchemeConsumer
        )
    }

    @Test
    fun test() {
        // prepare
        val outputModel = SelectedPaymentOptionOutputModel(paymentOption, false, false)
        on(selectPaymentOptionUseCase(paymentOption.id)).thenReturn(outputModel)

        // invoke
        selectPaymentOptionTokenizeSchemeSetter(paymentOption.id)

        // assert
        inOrder(selectPaymentOptionUseCase, tokenizeSchemeConsumer).apply {
            verify(selectPaymentOptionUseCase).invoke(paymentOption.id)
            verify(tokenizeSchemeConsumer).invoke(tokenizeScheme)
            verifyNoMoreInteractions()
        }
    }

    private interface TokenizeSchemeConsumer : (TokenizeScheme) -> Unit
}
