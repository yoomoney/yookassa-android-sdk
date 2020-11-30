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

import android.graphics.drawable.Drawable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.impl.contract.ContractSuccessViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractUserAuthRequiredViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.SavePaymentMethodViewModel
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentOptionViewModel
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.UserAuthRequired

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class ContractOpenedReporterTest {

    @Mock
    private lateinit var authType: AuthType
    @Mock
    private lateinit var tokenizeScheme: TokenizeScheme
    @Mock
    private lateinit var presenter: Presenter<SelectPaymentOptionOutputModel, ContractViewModel>
    @Mock
    private lateinit var mockReporter: Reporter

    private lateinit var reporter: ContractOpenedReporter

    @Before
    fun setUp() {
        reporter = ContractOpenedReporter({ authType }, { tokenizeScheme }, presenter, mockReporter)
    }

    @[Test Suppress("UNCHECKED_CAST")]
    fun `should send name, AuthType and TokenizeScheme`() {
        // prepare
        val testOutputModel = SelectedPaymentOptionOutputModel(
            createWalletPaymentOption(
                1
            ), false, false)
        val drawable = mock(Drawable::class.java)
        val testViewModel = ContractSuccessViewModel(
            "", "", PaymentOptionViewModel(1, drawable, "", ""), "", false, SavePaymentMethodViewModel.UserSelects, false, null, false
        )
        on(presenter(testOutputModel)).thenReturn(testViewModel)

        // invoke
        reporter(testOutputModel)

        // assert
        verify(mockReporter).report("screenPaymentContract", listOf(authType, tokenizeScheme))
    }

    @Test
    fun `should ignore reporting when OutputModel unsupported`() {
        // prepare
        val testOutputModel = UserAuthRequired()
        val drawable = mock(Drawable::class.java)
        val testViewModel = ContractSuccessViewModel(
            "", "", PaymentOptionViewModel(1, drawable, "", ""), "", false, SavePaymentMethodViewModel.UserSelects, false, null, false
        )
        on(presenter(testOutputModel)).thenReturn(testViewModel)

        // invoke
        reporter(testOutputModel)

        // assert
        verifyZeroInteractions(mockReporter)
    }

    @Test
    fun `should ignore reporting when ViewModel unsupported`() {
        // prepare
        val testOutputModel = SelectedPaymentOptionOutputModel(
            createWalletPaymentOption(
                1
            ), false, false)
        val testViewModel = ContractUserAuthRequiredViewModel
        on(presenter(testOutputModel)).thenReturn(testViewModel)

        // invoke
        reporter(testOutputModel)

        // assert
        verifyZeroInteractions(mockReporter)
    }

    @Test
    fun `should ignore reporting`() {
        // prepare
        val testOutputModel = UserAuthRequired()
        val testViewModel = ContractUserAuthRequiredViewModel
        on(presenter(testOutputModel)).thenReturn(testViewModel)

        // invoke
        reporter(testOutputModel)

        // assert
        verifyZeroInteractions(mockReporter)
    }
}
