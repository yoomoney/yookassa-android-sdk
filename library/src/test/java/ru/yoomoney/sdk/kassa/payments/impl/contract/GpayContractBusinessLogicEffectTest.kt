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

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.contract.Contract
import ru.yoomoney.sdk.kassa.payments.contract.ContractBusinessLogic
import ru.yoomoney.sdk.kassa.payments.contract.createGPayContractInfo
import ru.yoomoney.sdk.kassa.payments.createGooglePayPaymentOption
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.model.ShopProperties
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.utils.func
import ru.yoomoney.sdk.march.Logic
import java.lang.IllegalStateException
import java.math.BigDecimal
import kotlin.reflect.KClass

@RunWith(value = Parameterized::class)
internal class GpayContractBusinessLogicEffectTest(
    private val merchantSavePaymentMethod: SavePaymentMethod,
    private val backendSavePaymentMethod: Boolean,
    private val shouldSavePaymentMethod: Boolean,
    private val isSafeDeal: Boolean,
    private val isMarketplace: Boolean,
    private val fee: Fee?,
    private val excpectedState: KClass<Any>
) {

    private val showState: (Contract.State) -> Contract.Action = mock()
    private val showEffect: (Contract.Effect) -> Unit = mock()
    private val source: () -> Contract.Action = mock()
    private val logoutUseCase: LogoutUseCase = mock()
    private val getConfirmation: GetConfirmation = mock()
    private val shopPropertiesRepository: ShopPropertiesRepository = mock()

    private val paymentParameters = PaymentParameters(
        amount = Amount(BigDecimal.TEN, RUB),
        title = "title",
        subtitle = "subtitle",
        clientApplicationKey = "clientApplicationKey",
        shopId = "shopId",
        savePaymentMethod = merchantSavePaymentMethod,
        paymentMethodTypes = setOf(PaymentMethodType.GOOGLE_PAY),
        authCenterClientId = ""
    )

    private fun createLogic(paymentParameters: PaymentParameters? = null): Logic<Contract.State, Contract.Action> {
        return ContractBusinessLogic(
            showState = { showState(it) },
            showEffect = { showEffect(it) },
            source = { source() },
            paymentParameters = paymentParameters ?: this.paymentParameters,
            logoutUseCase = logoutUseCase,
            getConfirmation = getConfirmation,
            loadedPaymentOptionListRepository = mock(),
            selectPaymentMethodUseCase = mock(),
            userAuthInfoRepository = mock(),
            shopPropertiesRepository = shopPropertiesRepository
        )
    }
    @Test
    fun test() = runBlockingTest {
        // given
        val content = Contract.State.Content(
            shopTitle = paymentParameters.title,
            shopSubtitle = paymentParameters.subtitle,
            savePaymentMethod = merchantSavePaymentMethod,
            shouldSavePaymentMethod = shouldSavePaymentMethod,
            shouldSavePaymentInstrument = false,
            confirmation = NoConfirmation,
            contractInfo = createGPayContractInfo(backendSavePaymentMethod, createGooglePayPaymentOption(1, fee, backendSavePaymentMethod) as GooglePay),
            customerId = null,
            isSinglePaymentMethod = false,
            isSplitPayment = isSafeDeal || isMarketplace
        )
        val expected = when (excpectedState) {
            Contract.State.Content::class -> content
            Contract.State.GooglePay::class -> Contract.State.GooglePay(content, 1)
            else -> throw IllegalStateException("Work only with Contract.State.Content and Contract.State.GooglePay")
        }
        Mockito.`when`(getConfirmation.invoke(anyOrNull())).thenReturn(NoConfirmation)
        Mockito.`when`(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(isSafeDeal = isSafeDeal, isMarketplace = isMarketplace))
        val out =
            createLogic(paymentParameters = paymentParameters.copy(savePaymentMethod = merchantSavePaymentMethod)).invoke(
                Contract.State.Loading, Contract.Action.LoadContractSuccess(
                    SelectedPaymentMethodOutputModel(content.contractInfo.paymentOption, null, false)
                )
            )

        // when
        out.sources.func()

        // then
        verify(showState).invoke(expected)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: isValid({0}, {1}, {2}, {3}, {4})={5}")
        fun data(): Iterable<Array<out Any?>> {
            val fee = Fee(service = Amount(BigDecimal.TEN, RUB))
            return arrayListOf(
                // Without fee
                arrayOf(SavePaymentMethod.ON, true, true, true, false, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.ON, false, false, false, false, null, Contract.State.GooglePay::class),
                arrayOf(SavePaymentMethod.OFF, true, false, false, false, null, Contract.State.GooglePay::class),
                arrayOf(SavePaymentMethod.OFF, false, false, false, false, null, Contract.State.GooglePay::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, true, false, false, false, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, false, false, false, false, null, Contract.State.GooglePay::class),

                // With fee, should always show contract screen
                arrayOf(SavePaymentMethod.ON, true, true, false, true, fee, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.ON, true, true, false, true, fee, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.OFF, true, false, false, true, fee, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.OFF, false, false, false, true, fee, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, true, false, false, true, fee, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, false, false, false, true, fee, Contract.State.Content::class),

                // With split payment, should always show contract screen
                arrayOf(SavePaymentMethod.ON, true, true, false, false, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.ON, false, false, false, true, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.OFF, false, false, true, false, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.OFF, false, false, false, true, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, true, false, false, false, null, Contract.State.Content::class),
                arrayOf(SavePaymentMethod.USER_SELECTS, false, false, false, true, null, Contract.State.Content::class)
            ).toList()
        }
    }
}