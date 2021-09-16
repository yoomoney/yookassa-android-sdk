/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.contract

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State.Content
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State.Error
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State.GooglePay
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State.Loading
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.model.ShopProperties
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import java.math.BigDecimal
import java.util.Currency

@RunWith(Parameterized::class)
internal class ContractBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: Contract.State,
    val action: Action,
    val expected: Contract.State
) {
    private val paymentParameters = PaymentParameters(
        amount = Amount(BigDecimal.TEN, RUB),
        title = "shopTitle",
        subtitle = "shopSubtitle",
        clientApplicationKey = "clientApplicationKey",
        shopId = "shopId",
        savePaymentMethod = SavePaymentMethod.ON,
        authCenterClientId = "authCenterClientId"
    )

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val paymentOptionId = 11
            val errorState = Error(Throwable("Error state"))
            val loadingState = Loading
            val paymentOption = BankCardPaymentOption(
                paymentOptionId,
                Amount(
                    BigDecimal.TEN,
                    Currency.getInstance("RUB")
                ),
                null,
                false,
                emptyList(),
                emptyList(),
                false
            )
            val contentState = Content(
                shopTitle = "shopTitle",
                shopSubtitle = "shopSubtitle",
                shouldSavePaymentMethod = false,
                shouldSavePaymentInstrument = false,
                savePaymentMethod = SavePaymentMethod.ON,
                isSinglePaymentMethod = false,
                contractInfo = ContractInfo.NewBankCardContractInfo(paymentOption),
                confirmation = ExternalConfirmation,
                isSplitPayment = true,
                customerId = null
            )

            val loadContractSuccessAction = Action.LoadContractSuccess(
                SelectedPaymentMethodOutputModel(
                    paymentOption = paymentOption,
                    walletLinkingPossible = true,
                    instrument = null
                )
            )

            val contractLoadFailAction = Action.LoadContractFailed(errorState.error)
            val loadAction = Action.Load
            val logoutAction = Action.Logout
            val changeAllowWalletLinkingAction = Action.ChangeAllowWalletLinking(true)
            val changeSavePaymentMethod = Action.ChangeSavePaymentMethod(false)
            val googlePlayState = GooglePay(contentState, paymentOptionId)

            val paymentOptionInfo = NewCardInfo(
                number = "number",
                expirationMonth = "expirationMonth",
                expirationYear = "expirationYear",
                csc = "csc"
            )

            val tokenizePaymentInstrument = Action.TokenizePaymentInstrument(
                instrument = PaymentInstrumentBankCard(
                    paymentInstrumentId = "instrumentId",
                    last4 = "0000",
                    first6 = "000000",
                    cscRequired = true,
                    cardType = CardBrand.BANK_CARD
                ),
                csc = "csc"
            )
            val tokenizeAction = Action.Tokenize(paymentOptionInfo)
            val tokenizeCancelled = Action.TokenizeCancelled

            return generateBusinessLogicTests<Contract.State, Action>(
                generateState = {
                    when (it) {
                        Loading::class -> loadingState
                        Error::class -> errorState
                        Content::class -> contentState
                        GooglePay::class -> googlePlayState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.Load::class -> loadAction
                        Action.LoadContractFailed::class -> contractLoadFailAction
                        Action.LoadContractSuccess::class -> loadContractSuccessAction
                        Action.TokenizePaymentInstrument::class -> tokenizePaymentInstrument
                        Action.Tokenize::class -> tokenizeAction
                        Action.TokenizeCancelled::class -> tokenizeCancelled
                        Action.RestartProcess::class -> Action.RestartProcess
                        Action.Logout::class -> logoutAction
                        Action.ChangeAllowWalletLinking::class -> changeAllowWalletLinkingAction
                        Action.ChangeSavePaymentMethod::class -> changeSavePaymentMethod
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        contentState to loadAction -> loadingState
                        contentState to changeAllowWalletLinkingAction -> contentState
                        contentState to changeSavePaymentMethod -> contentState.copy(shouldSavePaymentMethod = false)
                        contentState to Action.GooglePayCancelled -> loadingState
                        loadingState to contractLoadFailAction -> errorState
                        loadingState to loadContractSuccessAction -> contentState.copy(shouldSavePaymentMethod = false)
                        errorState to loadAction -> loadingState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = ContractBusinessLogic(
        showState = mock(),
        showEffect = mock(),
        source = mock(),
        paymentParameters = paymentParameters,
        selectPaymentMethodUseCase = mock(),
        logoutUseCase = mock(),
        getConfirmation = object : GetConfirmation {
            override fun invoke(p1: PaymentOption): Confirmation {
                return ExternalConfirmation
            }
        },
        loadedPaymentOptionListRepository = mock(),
        shopPropertiesRepository = object: ShopPropertiesRepository{
            override var shopProperties: ShopProperties = ShopProperties(isSafeDeal = true, isMarketplace = true)
        },
        userAuthInfoRepository = mock()
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}