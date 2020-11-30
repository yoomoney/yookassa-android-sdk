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
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.impl.contract.ContractCompleteViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractErrorViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractViewModel
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ActionTokenizeReporterTest {

    @Mock
    private lateinit var userAuthTypeProvider: () -> AuthType
    @Mock
    private lateinit var userAuthTokenTypeProvider: () -> AuthTokenType
    @Mock
    private lateinit var presenter: Presenter<TokenizeOutputModel, ContractViewModel>
    @Mock
    private lateinit var reporter: Reporter

    private lateinit var actionTokenizeReporter: ActionTokenizeReporter

    @Before
    fun setUp() {
        actionTokenizeReporter =
                ActionTokenizeReporter(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter)
    }

    @Test
    fun shouldNotReport_When_NotTokenOutputModel_And_NotContractCompleteViewModel() {
        // prepare
        val outputModel = TokenizePaymentOptionInfoRequired(
            createNewCardPaymentOption(
                0
            ), true)
        on(presenter(outputModel)).thenReturn(ContractErrorViewModel("err"))

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldNotReport_When_TokenOutputModel_And_NotContractCompleteViewModel() {
        // prepare
        val outputModel = TokenOutputModel("test token",
            createWalletPaymentOption(0)
        )
        on(presenter(outputModel)).thenReturn(ContractErrorViewModel("err"))

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldNotReport_When_NotTokenOutputModel_And_ContractCompleteViewModel() {
        // prepare
        val outputModel = TokenizePaymentOptionInfoRequired(
            createNewCardPaymentOption(
                0
            ), true)
        on(presenter(outputModel))
            .thenReturn(ContractCompleteViewModel("test token", PaymentMethodType.YOO_MONEY))

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldReport_TokenizeSchemeWallet_When_WalletPaymentOption() {
        // prepare
        val outputModel = TokenOutputModel("test token",
            createWalletPaymentOption(0)
        )
        val authTypeParam = AuthTypeWithoutAuth()
        val authTokenTypeParam = AuthTokenTypeSingle()
        on(presenter(outputModel))
            .thenReturn(ContractCompleteViewModel("test token", PaymentMethodType.YOO_MONEY))
        on(userAuthTypeProvider()).thenReturn(authTypeParam)
        on(userAuthTokenTypeProvider()).thenReturn(authTokenTypeParam)

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(userAuthTypeProvider).invoke()
            verify(userAuthTokenTypeProvider).invoke()
            verify(reporter).report(
                "actionTokenize", listOf(TokenizeSchemeWallet(), authTypeParam, authTokenTypeParam)
            )
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldReport_TokenizeSchemeLinkedCard_When_LinkedCardPaymentOption() {
        // prepare
        val outputModel = TokenOutputModel("test token",
            createLinkedCardPaymentOption(0)
        )
        val authTypeParam = AuthTypeWithoutAuth()
        val authTokenTypeParam = AuthTokenTypeSingle()
        on(presenter(outputModel))
            .thenReturn(ContractCompleteViewModel("test token", PaymentMethodType.YOO_MONEY))
        on(userAuthTypeProvider()).thenReturn(authTypeParam)
        on(userAuthTokenTypeProvider()).thenReturn(authTokenTypeParam)

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(userAuthTypeProvider).invoke()
            verify(userAuthTokenTypeProvider).invoke()
            verify(reporter).report(
                "actionTokenize", listOf(TokenizeSchemeLinkedCard(), authTypeParam, authTokenTypeParam)
            )
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun shouldReport_TokenizeSchemeBankCard_When_NewCardPaymentOption() {
        // prepare
        val outputModel = TokenOutputModel("test token",
            createNewCardPaymentOption(0)
        )
        val authTypeParam = AuthTypeWithoutAuth()
        val authTokenTypeParam = AuthTokenTypeSingle()
        on(presenter(outputModel))
            .thenReturn(ContractCompleteViewModel("test token", PaymentMethodType.YOO_MONEY))
        on(userAuthTypeProvider()).thenReturn(authTypeParam)
        on(userAuthTokenTypeProvider()).thenReturn(authTokenTypeParam)

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(userAuthTypeProvider).invoke()
            verify(userAuthTokenTypeProvider).invoke()
            verify(reporter).report(
                "actionTokenize", listOf(TokenizeSchemeBankCard(), authTypeParam, authTokenTypeParam)
            )
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should report TokenizeSchemeSbolSms when NewCardPaymentOption`() {
        // prepare
        val outputModel = TokenOutputModel("test token",
            createSbolSmsInvoicingPaymentOption(0)
        )
        val authTypeParam = AuthTypeWithoutAuth()
        val authTokenTypeParam = AuthTokenTypeSingle()
        on(presenter(outputModel))
            .thenReturn(ContractCompleteViewModel("test token", PaymentMethodType.YOO_MONEY))
        on(userAuthTypeProvider()).thenReturn(authTypeParam)
        on(userAuthTokenTypeProvider()).thenReturn(authTokenTypeParam)

        // invoke
        actionTokenizeReporter(outputModel)

        // assert
        inOrder(userAuthTypeProvider, userAuthTokenTypeProvider, presenter, reporter).apply {
            verify(presenter).invoke(outputModel)
            verify(userAuthTypeProvider).invoke()
            verify(userAuthTokenTypeProvider).invoke()
            verify(reporter).report(
                "actionTokenize", listOf(TokenizeSchemeSbolSms(), authTypeParam, authTokenTypeParam)
            )
            verifyNoMoreInteractions()
        }
    }
}
