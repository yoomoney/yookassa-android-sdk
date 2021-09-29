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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.config
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.contract.Contract
import ru.yoomoney.sdk.kassa.payments.contract.ContractBusinessLogic
import ru.yoomoney.sdk.kassa.payments.contract.ContractInfo
import ru.yoomoney.sdk.kassa.payments.contract.createLinkedBankCardContractInfo
import ru.yoomoney.sdk.kassa.payments.contract.createNewBankCardContractInfo
import ru.yoomoney.sdk.kassa.payments.createBankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.ShopProperties
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInstrumentInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInputModel
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.savePaymentMethodOptionTexts
import ru.yoomoney.sdk.kassa.payments.utils.getAllPaymentMethods
import ru.yoomoney.sdk.march.Effect
import java.math.BigDecimal

internal class ContractBusinessLogicEffectTest {

    private val showState: (Contract.State) -> Contract.Action = mock()
    private val showEffect: (Contract.Effect) -> Unit = mock()
    private val source: () -> Contract.Action = mock()
    private val getConfirmation: GetConfirmation = mock()
    private val shopPropertiesRepository: ShopPropertiesRepository = mock()
    private val configRepository: ConfigRepository = mock()

    private val paymentOptionId = 123

    @Test
    fun `should tokenize payment instrument without csc`() {
        val paymentInstrumentBankCard = PaymentInstrumentBankCard(
            paymentInstrumentId = "paymentInstrumentId",
            last4 = "last4",
            first6 = "first6",
            cscRequired = false,
            cardType = CardBrand.MASTER_CARD
        )
        val contractInfo = createLinkedBankCardContractInfo(
            instrument = paymentInstrumentBankCard
        )
        val content = createContent(
            shouldSavePaymentMethod = true,
            contractInfo = contractInfo
        )

        val tokenizeInputModel = TokenizeInstrumentInputModel(
            paymentOptionId = content.contractInfo.paymentOption.id,
            savePaymentMethod = content.shouldSavePaymentMethod,
            instrumentBankCard = paymentInstrumentBankCard,
            allowWalletLinking = false,
            confirmation = content.confirmation,
            csc = null
        )
        val tokenizePaymentInstrument = Contract.Action.TokenizePaymentInstrument(paymentInstrumentBankCard, null)

        val logic = getLogic(getPaymentParameters(setOf(PaymentMethodType.BANK_CARD)))
        val out = logic(content, tokenizePaymentInstrument)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.ShowTokenize(tokenizeInputModel))
    }

    @Test
    fun `should tokenize payment instrument with csc`() {
        val paymentInstrumentBankCard = PaymentInstrumentBankCard(
            paymentInstrumentId = "paymentInstrumentId",
            last4 = "last4",
            first6 = "first6",
            cscRequired = true,
            cardType = CardBrand.MASTER_CARD
        )
        val contractInfo = createLinkedBankCardContractInfo(
            instrument = paymentInstrumentBankCard
        )
        val content = createContent(
            shouldSavePaymentMethod = true,
            contractInfo = contractInfo
        )

        val tokenizeInputModel = TokenizeInstrumentInputModel(
            paymentOptionId = content.contractInfo.paymentOption.id,
            savePaymentMethod = content.shouldSavePaymentMethod,
            instrumentBankCard = paymentInstrumentBankCard,
            allowWalletLinking = false,
            confirmation = content.confirmation,
            csc = "csc"
        )
        val tokenizePaymentInstrument = Contract.Action.TokenizePaymentInstrument(paymentInstrumentBankCard, "csc")

        val logic = getLogic(getPaymentParameters(setOf(PaymentMethodType.BANK_CARD)))
        val out = logic(content, tokenizePaymentInstrument)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.ShowTokenize(tokenizeInputModel))
    }

    @Test
    fun `should tokenize effect with expected parameters when Content and Tokenize Action`() {
        testTokenizeEffect(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = true,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = true
        )

        testTokenizeEffect(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = false,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = false
        )

        testTokenizeEffect(
            shouldSavePaymentMethod = false,
            shouldSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        testTokenizeEffect(
            shouldSavePaymentMethod = false,
            shouldSavePaymentInstrument = false,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = false
        )
    }

    @Test
    fun `should CancelProcess effect when GooglePayCancelled Action`() {
        val content = createContent(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = true
        )

        val googlePayState = Contract.State.GooglePay(content, paymentOptionId)
        val googlePayAction = Contract.Action.GooglePayCancelled

        val logic = getLogic(getPaymentParameters(setOf(PaymentMethodType.BANK_CARD)))
        val out = logic(googlePayState, googlePayAction)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.CancelProcess)
    }

    @Test
    fun `should RestartProcess effect when TokenizeCanceled Action`() {
        val content = createContent(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = true
        )

        val googlePayState = Contract.State.GooglePay(content, paymentOptionId)
        val tokenizeCancelledAction = Contract.Action.TokenizeCancelled

        val logic = getLogic()
        val out = logic(googlePayState, tokenizeCancelledAction)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.RestartProcess)
    }

    @Test
    fun `should StartGooglePay effect when TokenizeCanceled Action`() {
        val content = createContent(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = true
        )

        val googlePayState = Contract.State.GooglePay(content, paymentOptionId)
        val tokenizeCancelledAction = Contract.Action.TokenizeCancelled

        val logic = getLogic(getPaymentParameters(paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD)))
        val out = logic(googlePayState, tokenizeCancelledAction)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.CancelProcess)
    }

    @Test
    fun `should CancelProcess effect when TokenizeCanceled Action`() {
        val content = createContent(
            shouldSavePaymentMethod = true,
            shouldSavePaymentInstrument = true
        )

        val googlePayState = Contract.State.GooglePay(content, paymentOptionId)
        val tokenizeCancelledAction = Contract.Action.TokenizeCancelled

        val logic = getLogic(getPaymentParameters(paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD)))
        val out = logic(googlePayState, tokenizeCancelledAction)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(Contract.Effect.CancelProcess)
    }

    @Test
    fun `check ChangeSavePaymentMethod`() {
        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = true
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = false,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = false
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = false,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = false
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.ON,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.ON,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        checkChangeSavePaymentMethod(
            clientSavePaymentMethod = SavePaymentMethod.OFF,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )
    }

    private fun checkChangeSavePaymentMethod(
        clientSavePaymentMethod: SavePaymentMethod,
        apiSavePaymentMethod: Boolean,
        apiSavePaymentInstrument: Boolean,

        expectedSavePaymentMethod: Boolean,
        expectedSavePaymentInstrument: Boolean
    ) {
        // given
        val givenContent = createContent(
            savePaymentMethod = clientSavePaymentMethod,
            shouldSavePaymentMethod = false,
            shouldSavePaymentInstrument = false,
            contractInfo = createNewBankCardContractInfo(
                savePaymentMethodAllowed = apiSavePaymentMethod,
                createBinding = apiSavePaymentInstrument
            )
        )

        val expectedContent = createContent(
            savePaymentMethod = clientSavePaymentMethod,
            shouldSavePaymentMethod = expectedSavePaymentMethod,
            shouldSavePaymentInstrument = expectedSavePaymentInstrument,
            contractInfo = createNewBankCardContractInfo(
                savePaymentMethodAllowed = apiSavePaymentMethod,
                createBinding = apiSavePaymentInstrument
            )
        )

        val changeSavePaymentMethod = Contract.Action.ChangeSavePaymentMethod(savePaymentMethod = true)

        whenever(getConfirmation.invoke(any())).thenReturn(NoConfirmation)

        val logic = getLogic(
            paymentParameters = getPaymentParameters(
                clientSavePaymentMethod = clientSavePaymentMethod,
                customerId = "customerId"
            )
        )
        val out = logic(givenContent, changeSavePaymentMethod)
        // when
        runBlocking { out.sources.func() }

        // then
        //verify(showState).invoke(expectedContent)
        assertEquals(expectedContent, out.state)
    }

    private fun testTokenizeEffect(
        shouldSavePaymentMethod: Boolean,
        shouldSavePaymentInstrument: Boolean,
        expectedSavePaymentMethod: Boolean,
        expectedSavePaymentInstrument: Boolean
    ) {
        // given
        val content = createContent(
            shouldSavePaymentMethod = shouldSavePaymentMethod,
            shouldSavePaymentInstrument = shouldSavePaymentInstrument
        )
        val tokenizeEffect = Contract.Effect.ShowTokenize(
            tokenizeInputModel = TokenizePaymentOptionInputModel(
                paymentOptionId = 1,
                savePaymentMethod = expectedSavePaymentMethod,
                savePaymentInstrument = expectedSavePaymentInstrument,
                confirmation = NoConfirmation,
                paymentOptionInfo = null,
                allowWalletLinking = false
            )
        )
        val tokenize = Contract.Action.Tokenize()

        val logic = getLogic()
        val out = logic(content, tokenize)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(tokenizeEffect)
    }


    @Test
    fun `check initial shouldSavePaymentMethod and shouldSavePaymentInstrument flags`() {
        whenever(shopPropertiesRepository.shopProperties).thenReturn(ShopProperties(false, false))
        whenever(configRepository.getConfig()).thenReturn(config)
        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.ON,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = true
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.ON,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.ON,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = false,
            expectedSavePaymentMethod = true,
            expectedSavePaymentInstrument = false
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = false
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.USER_SELECTS,
            apiSavePaymentMethod = false,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.OFF,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = true,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = true
        )

        `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
            clientSavePaymentMethod = SavePaymentMethod.OFF,
            apiSavePaymentMethod = true,
            apiSavePaymentInstrument = false,
            expectedSavePaymentMethod = false,
            expectedSavePaymentInstrument = false
        )
    }

    private fun `check content shouldSavePaymentMethod shouldSavePaymentInstrument flags`(
        clientSavePaymentMethod: SavePaymentMethod,
        apiSavePaymentMethod: Boolean,
        apiSavePaymentInstrument: Boolean,

        expectedSavePaymentMethod: Boolean,
        expectedSavePaymentInstrument: Boolean
    ) {
        // given
        val content = createContent(
            shouldSavePaymentMethod = expectedSavePaymentMethod,
            savePaymentMethod = clientSavePaymentMethod,
            shouldSavePaymentInstrument = expectedSavePaymentInstrument,
            contractInfo = createNewBankCardContractInfo(
                savePaymentMethodAllowed = apiSavePaymentMethod,
                createBinding = apiSavePaymentInstrument
            )
        )

        val loadSuccess = Contract.Action.LoadContractSuccess(
            SelectedPaymentMethodOutputModel(
                paymentOption = createBankCardPaymentOption(
                    id = 1,
                    instruments = emptyList(),
                    savePaymentMethodAllowed = apiSavePaymentMethod,
                    savePaymentInstrument = apiSavePaymentInstrument
                ),
                instrument = null,
                walletLinkingPossible = false
            )
        )

        whenever(getConfirmation.invoke(any())).thenReturn(NoConfirmation)

        val logic = getLogic(
            paymentParameters = getPaymentParameters(
                clientSavePaymentMethod = clientSavePaymentMethod,
                customerId = "customerId"
            )
        )
        val out = logic(Contract.State.Loading, loadSuccess)

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showState).invoke(content)
    }

    private fun createContent(
        isSinglePaymentMethod: Boolean = false,
        shouldSavePaymentMethod: Boolean = true,
        shouldSavePaymentInstrument: Boolean = true,
        savePaymentMethod: SavePaymentMethod = SavePaymentMethod.ON,
        contractInfo: ContractInfo = createNewBankCardContractInfo(),
        confirmation: Confirmation = NoConfirmation,
        customerId: String? = "customerId"
    ): Contract.State.Content {
        val paymentParameters = getPaymentParameters()
        return Contract.State.Content(
            shopTitle = paymentParameters.title,
            shopSubtitle = paymentParameters.subtitle,
            isSinglePaymentMethod = isSinglePaymentMethod,
            shouldSavePaymentMethod = shouldSavePaymentMethod,
            shouldSavePaymentInstrument = shouldSavePaymentInstrument,
            savePaymentMethod = savePaymentMethod,
            contractInfo = contractInfo,
            confirmation = confirmation,
            isSplitPayment = false,
            customerId = customerId,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            userAgreementUrl = "Нажимая кнопку, вы принимаете <a href='https://yoomoney.ru/page?id=526623'>условия сервиса</>"
        )
    }

    private fun getLogic(
        paymentParameters: PaymentParameters = getPaymentParameters(getAllPaymentMethods())
    ) = ContractBusinessLogic(
        showState = { showState(it) },
        showEffect = { showEffect(it) },
        source = { source() },
        paymentParameters = paymentParameters,
        selectPaymentMethodUseCase = mock(),
        logoutUseCase = mock(),
        getConfirmation = getConfirmation,
        loadedPaymentOptionListRepository = mock(),
        shopPropertiesRepository = shopPropertiesRepository,
        userAuthInfoRepository = mock(),
        configRepository = configRepository
    )

    private fun getPaymentParameters(
        paymentMethodTypes: Set<PaymentMethodType> = getAllPaymentMethods(),
        clientSavePaymentMethod: SavePaymentMethod = SavePaymentMethod.ON,
        customerId: String? = null
    ) = PaymentParameters(
        amount = Amount(BigDecimal.ONE, RUB),
        title = "title",
        subtitle = "subtitle",
        clientApplicationKey = "clientApplicationKey",
        shopId = "shopId",
        savePaymentMethod = clientSavePaymentMethod,
        authCenterClientId = "authCenterClientId",
        paymentMethodTypes = paymentMethodTypes,
        customerId = customerId
    )

    private suspend fun <E> List<Effect<E>>.func() {
        forEach {
            when (it) {
                is Effect.Input.Fun -> it.func()
                is Effect.Output -> it.func()
                else -> error("unexpected")
            }
        }
    }
}