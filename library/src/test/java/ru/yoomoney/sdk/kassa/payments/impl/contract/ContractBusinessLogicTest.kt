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

package ru.yoomoney.sdk.kassa.payments.impl.contract

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.contract.Contract
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State.*
import ru.yoomoney.sdk.kassa.payments.contract.ContractBusinessLogic
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.NewCard
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.march.generateBusinessLogicTests
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
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
            val paymentOption = NewCard(paymentOptionId, Amount(BigDecimal.TEN, Currency.getInstance("RUB")),null, false, emptyList())
            val contentState = Content(
                shopTitle = "shopTitle",
                shopSubtitle = "shopSubtitle",
                paymentOption = paymentOption,
                savePaymentMethod = false,
                showAllowWalletLinking = true,
                allowWalletLinking = true,
                confirmation = ExternalConfirmation
            )

            val loadContractSuccessAction = Action.LoadContractSuccess(
                SelectedPaymentOptionOutputModel(
                    paymentOption = paymentOption,
                    walletLinkingPossible = true
                )
            )

            val contractLoadFailAction = Action.LoadContractFailed(errorState.error)
            val loadAction = Action.Load
            val logoutAction = Action.Logout
            val changeAllowWalletLinkingAction =  Action.ChangeAllowWalletLinking(true)
            val changeSavePaymentMethod =  Action.ChangeSavePaymentMethod(true)

            val tokenOutputModel = TokenOutputModel(token= "Token", option = paymentOption)
            val googlePlayState = GooglePay(contentState, paymentOptionId)

            val paymentOptionInfo = NewCardInfo(
                number = "number",
                expirationMonth = "expirationMonth",
                expirationYear = "expirationYear",
                csc = "csc"
            )
            val tokenizeInputModel = TokenizeInputModel(
                paymentOptionId = paymentOptionId,
                savePaymentMethod = false,
                paymentOptionInfo = paymentOptionInfo,
                confirmation = ExternalConfirmation
            )

            val tokenizeAction = Action.Tokenize(paymentOptionInfo)
            val tokenizeSuccessAction = Action.TokenizeSuccess(tokenOutputModel)
            val tokenizeFailedAction = Action.TokenizeFailed(errorState.error)

            val tokenizeState = Tokenize(contentState, paymentOptionInfo)
            val tokenizeErrorState = TokenizeError(contentState, paymentOptionInfo, tokenizeFailedAction.error)

            val paymentAuthRequiredAction = Action.PaymentAuthRequired(paymentOption.charge)
            val paymentAuthSuccessAction = Action.PaymentAuthSuccess
            val paymentAuthCancelAction = Action.PaymentAuthCancel

            return generateBusinessLogicTests<Contract.State, Action>(
                generateState = {
                    when (it) {
                        Loading::class -> loadingState
                        Error::class -> errorState
                        Content::class -> contentState
                        Tokenize::class -> tokenizeState
                        TokenizeError::class -> tokenizeErrorState
                        GooglePay::class -> googlePlayState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.Load::class -> loadAction
                        Action.LoadContractFailed::class -> contractLoadFailAction
                        Action.LoadContractSuccess::class -> loadContractSuccessAction
                        Action.Tokenize::class -> tokenizeAction
                        Action.TokenizeFailed::class -> tokenizeFailedAction
                        Action.TokenizeSuccess::class -> Action.TokenizeSuccess(tokenOutputModel)
                        Action.RestartProcess::class -> Action.RestartProcess
                        Action.PaymentAuthRequired::class -> paymentAuthRequiredAction
                        Action.PaymentAuthSuccess::class -> paymentAuthSuccessAction
                        Action.PaymentAuthCancel::class -> paymentAuthCancelAction
                        Action.Logout::class -> logoutAction
                        Action.ChangeAllowWalletLinking::class -> changeAllowWalletLinkingAction
                        Action.ChangeSavePaymentMethod::class -> changeSavePaymentMethod
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        contentState to loadAction -> loadingState
                        contentState to tokenizeAction -> tokenizeState
                        contentState to changeAllowWalletLinkingAction -> contentState
                        contentState to changeSavePaymentMethod -> contentState.copy(savePaymentMethod = true)
                        contentState to Action.GooglePayCancelled -> loadingState

                        tokenizeState to tokenizeFailedAction -> tokenizeErrorState
                        tokenizeState to paymentAuthCancelAction -> contentState

                        tokenizeErrorState to tokenizeAction -> tokenizeState

                        loadingState to contractLoadFailAction -> errorState
                        loadingState to loadContractSuccessAction -> contentState.copy(savePaymentMethod = true)

                        errorState to loadAction -> loadingState

                        googlePlayState to tokenizeAction -> tokenizeState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = ContractBusinessLogic(mock(), mock(), mock(), paymentParameters, mock(), mock(), mock(), { ExternalConfirmation })

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}