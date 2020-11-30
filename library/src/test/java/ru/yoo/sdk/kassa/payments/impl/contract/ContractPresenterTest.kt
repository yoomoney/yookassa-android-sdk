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

package ru.yoo.sdk.kassa.payments.impl.contract

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.SavePaymentMethod
import ru.yoo.sdk.kassa.payments.createGooglePayPaymentOptionWithFee
import ru.yoo.sdk.kassa.payments.createGooglePayPaymentOptionWithoutFee
import ru.yoo.sdk.kassa.payments.createLinkedCardPaymentOption
import ru.yoo.sdk.kassa.payments.createNewCardPaymentOption
import ru.yoo.sdk.kassa.payments.createSbolSmsInvoicingPaymentOption
import ru.yoo.sdk.kassa.payments.createWalletPaymentOption
import ru.yoo.sdk.kassa.payments.impl.extensions.RUB
import ru.yoo.sdk.kassa.payments.impl.extensions.initExtensions
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.ProcessPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.RequestPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.SmsSessionRetryProgressViewModel
import ru.yoo.sdk.kassa.payments.model.AuthType
import ru.yoo.sdk.kassa.payments.model.AuthTypeState
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.UserAuthRequired
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentAuthRequiredOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthSuccessOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthWrongAnswerOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryOutputModel
import ru.yoo.sdk.kassa.payments.savePaymentMethodMessage
import ru.yoo.sdk.kassa.payments.savePaymentMethodViewModelTurnOn
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class ContractPresenterTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val context = RuntimeEnvironment.application

    private val charge = Amount(BigDecimal.TEN, RUB)
    private val shopTitle: CharSequence = "Shop name"
    private val shopSubtitle: CharSequence = "Shop info"

    private val hasAnotherOption = false
    private val testModel = SelectedPaymentOptionOutputModel(
        createNewCardPaymentOption(
            1
        ), hasAnotherOption, false)

    @Before
    fun setUp() {
        initExtensions(context)
    }

    @Test
    fun shouldShow_SelectedPaymentOption() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        val viewModel = presenter(testModel) as ContractSuccessViewModel

        // assert
        assertThat(viewModel.shopTitle, equalTo(shopTitle))
        assertThat(viewModel.shopSubtitle, equalTo(shopSubtitle))
        assertThat(viewModel.paymentOption.optionId, equalTo(testModel.paymentOption.id))
        assertThat(viewModel.showChangeButton, equalTo(hasAnotherOption))
        assertThat(
            viewModel.savePaymentMethodViewModel as SavePaymentMethodViewModel.On,
            equalTo(savePaymentMethodViewModelTurnOn)
        )
        assertThat(viewModel.showAllowWalletLinking, equalTo(false))
        assertThat(viewModel.paymentAuth, nullValue())
    }

    @Test
    fun `should show ContractSuccessViewModel when GooglePay selected and fee is zero`() {
        // prepare
        val testModel =
            SelectedPaymentOptionOutputModel(
                createGooglePayPaymentOptionWithoutFee(
                    1
                ), hasAnotherOption, false)
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        val viewModel = presenter(testModel)

        // assert
        assertThat(viewModel, instanceOf(GooglePayContractViewModel::class.java))
    }

    @Test
    fun `should show GooglePayContractViewModel when GooglePay selected and fee is not zero`() {
        // prepare
        val testModel =
            SelectedPaymentOptionOutputModel(
                createGooglePayPaymentOptionWithFee(
                    1
                ), hasAnotherOption, false)
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        val viewModel = presenter(testModel)

        // assert
        assertThat(viewModel, instanceOf(ContractSuccessViewModel::class.java))
    }

    @Test
    fun shouldReturn_ContractUserAuthRequired_When_UserAuthRequired() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        val viewModel = presenter(UserAuthRequired())

        // assert
        assertThat(viewModel, equalTo(ContractUserAuthRequiredViewModel as ContractViewModel))
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_PaymentAuthRequired_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(TokenizePaymentAuthRequiredOutputModel(charge))

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthStart_When_PaymentAuthRequired() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(TokenizePaymentAuthRequiredOutputModel(charge)) as ContractSuccessViewModel

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthStartViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthStart_When_PaymentAuthRequired_And_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(TokenizePaymentAuthRequiredOutputModel(charge)) as ContractSuccessViewModel

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthStartViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthStart_When_PaymentAuthRequired_And_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(TokenizePaymentAuthRequiredOutputModel(charge)) as ContractSuccessViewModel

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthStartViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_RequestPaymentAuthProgress_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(RequestPaymentAuthProgressViewModel)

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_RequestPaymentAuthProgress() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(RequestPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_RequestPaymentAuthProgress_And_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(RequestPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_RequestPaymentAuthProgress_And_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(RequestPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_ProcessPaymentAuthProgress_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context, shopTitle, shopSubtitle, SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(ProcessPaymentAuthProgressViewModel)

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProcessPaymentAuthProgress() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(ProcessPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProcessPaymentAuthProgress_And_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(ProcessPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProcessPaymentAuthProgress_And_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(ProcessPaymentAuthProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_ProgressSmsSessionRetry_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(SmsSessionRetryProgressViewModel)

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProgressSmsSessionRetry() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(SmsSessionRetryProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProgressSmsSessionRetry_And_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(SmsSessionRetryProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthProgress_When_ProgressSmsSessionRetry_And_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(SmsSessionRetryProgressViewModel)

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthProgressViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_PaymentAuthOutput_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    15
                )
            )
        )

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_PaymentAuthOutput() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(viewModel.paymentAuth.error, nullValue())
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthForm_WithoutRetry_When_PaymentAuthOutput_And_AuthType_isTotp() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.TOTP,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormNoRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.error, nullValue())
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_PaymentAuthOutput_And_RecurringPayment() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(viewModel.paymentAuth.error, nullValue())
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_PaymentAuthOutput_And_WalletLinking() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(viewModel.paymentAuth.error, nullValue())
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_ProcessPaymentAuthSuccess_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(ProcessPaymentAuthSuccessOutputModel())

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthSuccess_When_ProcessPaymentAuthSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(ProcessPaymentAuthSuccessOutputModel())

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthSuccessViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthSuccess_When_ProcessPaymentAuthSuccess_And_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(ProcessPaymentAuthSuccessOutputModel())

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthSuccessViewModel::class.java))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthSuccess_When_ProcessPaymentAuthSuccess_And_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(ProcessPaymentAuthSuccessOutputModel())

        // assert
        assertThat(viewModel.paymentAuth, instanceOf(PaymentAuthSuccessViewModel::class.java))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_ProcessPaymentAuthWrongAnswer_Without_ContractSuccess() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(ProcessPaymentAuthWrongAnswerOutputModel("test"))

        // assert that exception thrown
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_ProcessPaymentAuthWrongAnswer_Without_PaymentAuthForm() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        presenter(ProcessPaymentAuthWrongAnswerOutputModel("test"))

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthFormWithError_When_ProcessPaymentAuthWrongAnswer() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)
        val viewModelWithForm = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    1
                )
            )
        )
        viewModelWithForm.paymentAuth as PaymentAuthFormRetryViewModel

        // invoke
        val viewModel = presenter(
            ProcessPaymentAuthWrongAnswerOutputModel(
                "test"
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, equalTo(viewModelWithForm.paymentAuth.hint))
        assertThat(viewModel.paymentAuth.timeout, equalTo(viewModelWithForm.paymentAuth.timeout))
        assertThat(viewModel.paymentAuth.error, equalTo(context.getText(R.string.ym_wrong_passcode_error)))
    }

    @Test
    fun shouldReturn_PaymentAuthFormWithError_When_ProcessPaymentAuthWrongAnswer_With_RecurringPayment() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)
        val viewModelWithForm = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    1
                )
            )
        )
        viewModelWithForm.paymentAuth as PaymentAuthFormRetryViewModel

        // invoke
        val viewModel = presenter(
            ProcessPaymentAuthWrongAnswerOutputModel(
                "test"
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, equalTo(viewModelWithForm.paymentAuth.hint))
        assertThat(viewModel.paymentAuth.timeout, equalTo(viewModelWithForm.paymentAuth.timeout))
        assertThat(viewModel.paymentAuth.error, equalTo(context.getText(R.string.ym_wrong_passcode_error)))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthFormWithError_When_ProcessPaymentAuthWrongAnswer_With_WalletLinking() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))
        val viewModelWithForm = presenter(
            RequestPaymentAuthOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    1
                )
            )
        )
        viewModelWithForm.paymentAuth as PaymentAuthFormRetryViewModel

        // invoke
        val viewModel = presenter(
            ProcessPaymentAuthWrongAnswerOutputModel(
                "test"
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, equalTo(viewModelWithForm.paymentAuth.hint))
        assertThat(viewModel.paymentAuth.timeout, equalTo(viewModelWithForm.paymentAuth.timeout))
        assertThat(viewModel.paymentAuth.error, equalTo(context.getText(R.string.ym_wrong_passcode_error)))
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun shouldThrow_Exception_When_SmsSessionRetryOutput_Without_ContractSuccess() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )

        // invoke
        presenter(
            SmsSessionRetryOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert that exception thrown
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_SmsSessionRetryOutput() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(
            SmsSessionRetryOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_SmsSessionRetryOutput_And_RecurringPayment() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.ON,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel)

        // invoke
        val viewModel = presenter(
            SmsSessionRetryOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(
            "should show recurring payment",
            viewModel.savePaymentMethodViewModel == savePaymentMethodViewModelTurnOn
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_PaymentAuthForm_When_SmsSessionRetryOutput_And_WalletLinking() {
        // prepare
        val timeout = 15
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        presenter(testModel.copy(walletLinkingPossible = true))

        // invoke
        val viewModel = presenter(
            SmsSessionRetryOutputModel(
                AuthTypeState(
                    AuthType.SMS,
                    timeout
                )
            )
        )

        // assert
        viewModel.paymentAuth as PaymentAuthFormRetryViewModel
        assertThat(viewModel.paymentAuth.hint, notNullValue())
        assertThat(viewModel.paymentAuth.timeout, equalTo(timeout))
        assertThat(
            "should not show recurring payment",
            viewModel.savePaymentMethodViewModel == SavePaymentMethodViewModel.UserSelects
        )
        assertThat("should not show wallet linking", !viewModel.showAllowWalletLinking)
    }

    @Test
    fun shouldReturn_ContractDone_When_TokenOutput_With_Wallet() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        val output = TokenOutputModel("token",
            createWalletPaymentOption(1)
        )

        // invoke
        val viewModel = presenter(output) as ContractCompleteViewModel

        // assert
        assertThat(viewModel.token, equalTo(output.token))
        assertThat(viewModel.type, equalTo(PaymentMethodType.YOO_MONEY))
    }

    @Test
    fun shouldReturn_ContractDone_When_TokenOutput_With_LinkedCard() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        val output = TokenOutputModel("token",
            createLinkedCardPaymentOption(1)
        )

        // invoke
        val viewModel = presenter(output) as ContractCompleteViewModel

        // assert
        assertThat(viewModel.token, equalTo(output.token))
        assertThat(viewModel.type, equalTo(PaymentMethodType.YOO_MONEY))
    }

    @Test
    fun shouldReturn_ContractDone_When_TokenOutput_With_BankCard() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        val output = TokenOutputModel("token",
            createNewCardPaymentOption(1)
        )

        // invoke
        val viewModel = presenter(output) as ContractCompleteViewModel

        // assert
        assertThat(viewModel.token, equalTo(output.token))
        assertThat(viewModel.type, equalTo(PaymentMethodType.BANK_CARD))
    }

    @Test
    fun shouldReturn_ContractDone_When_TokenOutput_With_SbolSmsInvoicing() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        val output = TokenOutputModel("token",
            createSbolSmsInvoicingPaymentOption(1)
        )

        // invoke
        val viewModel = presenter(output) as ContractCompleteViewModel

        // assert
        assertThat(viewModel.token, equalTo(output.token))
        assertThat(viewModel.type, equalTo(PaymentMethodType.SBERBANK))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw IllegalArgumentException when PaymentOptionInfoRequired`() {
        // prepare
        val presenter = ContractPresenter(
            context,
            shopTitle,
            shopSubtitle,
            SavePaymentMethod.OFF,
            savePaymentMethodMessage,
            savePaymentMethodMessage
        )
        val output = TokenizePaymentOptionInfoRequired(
            createNewCardPaymentOption(
                1
            ), true)

        // invoke
        presenter(output)

        // assert that IllegalArgumentException thrown
    }
}
