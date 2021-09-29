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

package ru.yoomoney.sdk.kassa.payments.payment.loadOptionList

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.collection.IsIterableContainingInOrder.contains
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.CardInfo
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionsResponse
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.ShopProperties
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.on
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListNoWalletOutputModel
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListSuccessOutputModel
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCaseImpl
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class PaymentOptionsListUseCaseTest {

    private val testUser = AuthorizedUser()
    private val testInputModel = Amount(BigDecimal.TEN, RUB)
    private val testCharge =
        Amount(testInputModel.value + BigDecimal.ONE, RUB)
    private val testFee = Fee(
        service = Amount(BigDecimal.ONE, RUB),
        counterparty = Amount(BigDecimal("0.5"), RUB)
    )
    private val testPaymentMethodId = "test id"
    private val availableOptions = mutableListOf(
        BankCardPaymentOption(
            0,
            testCharge,
            testFee,
            null,
            null,
            true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            paymentInstruments = emptyList(),
            savePaymentInstrument = false
        ),
        Wallet(
            id = 1,
            charge = testCharge,
            fee = testFee,
            walletId = "123456787654321",
            balance = Amount(BigDecimal.TEN, RUB),
            icon = null,
            title = null,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false
        ),
        AbstractWallet(
            id = 2,
            charge = testCharge,
            fee = testFee,
            icon = null,
            title = null,
            savePaymentMethodAllowed = false,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        , savePaymentInstrument = false),
        LinkedCard(
            id = 3,
            charge = testCharge,
            fee = testFee,
            icon = null,
            title = null,
            cardId = "1234567887654321",
            brand = CardBrand.VISA,
            pan = "123456787654321",
            name = null,
            isLinkedToWallet = false,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false
        ),
        LinkedCard(
            id = 4,
            charge = testCharge,
            fee = testFee,
            icon = null,
            title = null,
            cardId = "1234567887654321",
            brand = CardBrand.VISA,
            pan = "123456787654321",
            name = "test name",
            isLinkedToWallet = false,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT),
            savePaymentInstrument = false
        ),
        SberBank(
            id = 5,
            charge = testCharge,
            fee = testFee,
            icon = null,
            title = null,
            savePaymentMethodAllowed = false,
            confirmationTypes = listOf(
                ConfirmationType.REDIRECT,
                ConfirmationType.EXTERNAL,
                ConfirmationType.MOBILE_APPLICATION
            ),
            savePaymentInstrument = false
        ),
        GooglePay(6, testCharge, testFee, null, null,false, emptyList(), false)
    )
    private val bankCardInfo: PaymentMethodBankCard =
        PaymentMethodBankCard(
            type = PaymentMethodType.BANK_CARD,
            id = "123123",
            saved = true,
            cscRequired = true,
            title = "title",
            card = CardInfo(
                first = "123456",
                last = "7890",
                expiryMonth = "10",
                expiryYear = "20",
                cardType = CardBrand.MASTER_CARD,
                source = PaymentMethodType.GOOGLE_PAY
            )
        )

    private val restrictions = mutableSetOf<PaymentMethodType>()

    @Mock
    private lateinit var paymentOptionListRepository: PaymentOptionListRepository

    @Mock
    private lateinit var currentUserRepository: CurrentUserRepository

    @Mock
    private lateinit var saveLoadedPaymentOptionsListRepository: SaveLoadedPaymentOptionsListRepository

    @Mock
    private lateinit var paymentMethodInfoGateway: PaymentMethodInfoGateway

    @Mock
    private lateinit var googlePayRepository: GooglePayRepository
    private lateinit var useCase: PaymentOptionsListUseCaseImpl

    @Before
    fun setUp() {
        on(
            paymentMethodInfoGateway.getPaymentMethodInfo(
                testPaymentMethodId
            )
        ).thenReturn(Result.Success(bankCardInfo))
        on(currentUserRepository.currentUser).thenReturn(testUser)
        on(
            paymentOptionListRepository.getPaymentOptions(
                testInputModel,
                testUser
            )
        ).thenReturn(Result.Success(
            PaymentOptionsResponse(
                paymentOptions = availableOptions,
                shopProperties = ShopProperties(isSafeDeal = true, isMarketplace = false)
            )
        )
        )

        on(googlePayRepository.checkGooglePayAvailable()).thenReturn(true)

        useCase = PaymentOptionsListUseCaseImpl(
            paymentOptionListRestrictions = restrictions,
            paymentOptionListRepository = paymentOptionListRepository,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            saveLoadedPaymentOptionsListRepository = saveLoadedPaymentOptionsListRepository,
            currentUserRepository = currentUserRepository,
            googlePayRepository = googlePayRepository,
            loadedPaymentOptionListRepository = mock(),
            paymentMethodRepository = mock(),
            shopPropertiesRepository = mock()
        )
    }

    @Test
    fun `should return SavedLinkedCard when paymentMethodId is set`() = runBlocking {
        // prepare
        val savedLinkedCard = listOf<PaymentOption>(
            PaymentIdCscConfirmation(
                id = 0,
                charge = testCharge,
                fee = testFee,
                icon = null,
                title = null,
                paymentMethodId = testPaymentMethodId,
                first = "123456",
                last = "7890",
                expiryYear = "20",
                expiryMonth = "10",
                savePaymentMethodAllowed = true,
                confirmationTypes = listOf(ConfirmationType.REDIRECT),
                brand = CardBrand.MASTER_CARD,
                savePaymentInstrument = false
            )
        )

        // invoke
        val action = useCase.loadPaymentOptions(
            testInputModel,
            testPaymentMethodId
        ) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(instanceOf(PaymentIdCscConfirmation::class.java)))

        val captor = argumentCaptor<List<PaymentOption>>()

        inOrder(
            paymentOptionListRepository,
            currentUserRepository,
            paymentMethodInfoGateway,
            saveLoadedPaymentOptionsListRepository
        ).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(paymentMethodInfoGateway).getPaymentMethodInfo(testPaymentMethodId)
            verify(saveLoadedPaymentOptionsListRepository).saveLoadedPaymentOptionsList(captor.capture())
            verifyNoMoreInteractions()
        }

        assertThat(captor.firstValue, equalTo(savedLinkedCard))
    }

    @Test
    fun `should return payment option list when Wallet not present`() = runBlocking<Unit> {
        // prepare
        availableOptions.removeIf { it is Wallet }

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(*availableOptions.toTypedArray()))

        inOrder(
            paymentOptionListRepository,
            paymentOptionListRepository,
            currentUserRepository,
            saveLoadedPaymentOptionsListRepository
        ).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when Wallet present`() = runBlocking<Unit> {
        // prepare

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(*availableOptions.toTypedArray()))

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return NewCard when bank card restriction`() = runBlocking<Unit> {
        // prepare
        restrictions.add(PaymentMethodType.BANK_CARD)

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(instanceOf(BankCardPaymentOption::class.java)))

        inOrder(paymentOptionListRepository, currentUserRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return GooglePay when google pay restriction`() = runBlocking<Unit> {
        // prepare
        restrictions.add(PaymentMethodType.GOOGLE_PAY)

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options.size, equalTo(1))
        assertThat(options[0], instanceOf(GooglePay::class.java))

        inOrder(paymentOptionListRepository, currentUserRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return SbolSmsInvoicing when sberbank restriction`() = runBlocking<Unit> {
        // prepare
        restrictions.add(PaymentMethodType.SBERBANK)

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(instanceOf(SberBank::class.java)))

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is SberBank })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when yoo money restriction`() = runBlocking<Unit> {
        // prepare
        restrictions.add(PaymentMethodType.YOO_MONEY)

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(
            options,
            contains(
                instanceOf(Wallet::class.java),
                instanceOf(AbstractWallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java)
            )
        )

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YooMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should show no wallet message when yoo money restriction and no Wallet`() = runBlocking<Unit> {
        // prepare
        restrictions.add(PaymentMethodType.YOO_MONEY)
        availableOptions.removeIf { it is Wallet }

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        // assert
        assertThat(action.content, instanceOf(PaymentOptionListNoWalletOutputModel::class.java))

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YooMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should show no wallet message when all restrictions set and no Wallet`() = runBlocking<Unit> {
        // prepare
        restrictions.addAll(PaymentMethodType.values())
        availableOptions.removeIf { it is Wallet }

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess

        // assert
        assertThat(action.content, instanceOf(PaymentOptionListNoWalletOutputModel::class.java))

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository)
                .saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when all restrictions set and Wallet present`() = runBlocking<Unit> {
        // prepare
        restrictions.addAll(PaymentMethodType.values())

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options
        // assert
        assertThat(
            options,
            contains(
                instanceOf(BankCardPaymentOption::class.java),
                instanceOf(Wallet::class.java),
                instanceOf(AbstractWallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(SberBank::class.java),
                instanceOf(GooglePay::class.java)
            )
        )

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `test all restrictions enabled equal to no restriction set`() = runBlocking<Unit> {
        // prepare noRestrictionsOutput

        // invoke noRestrictionsOutput
        val noRestrictionsAction =
            useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val noRestrictionsOptions = (noRestrictionsAction.content as PaymentOptionListSuccessOutputModel).options
        // prepare fullRestrictionsOutput
        restrictions.addAll(PaymentMethodType.values())

        // invoke fullRestrictionsOutput
        val fullRestrictionsAction =
            useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val fullRestrictionsOption = (fullRestrictionsAction.content as PaymentOptionListSuccessOutputModel).options
        // assert
        assertThat(noRestrictionsOptions, contains(*fullRestrictionsOption.toTypedArray()))
    }

    @Test
    fun `should throw Exception when empty payment option list`() = runBlocking<Unit> {
        // prepare
        on(
            paymentOptionListRepository.getPaymentOptions(
                testInputModel,
                testUser
            )
        ).thenReturn(Result.Success(PaymentOptionsResponse(
            paymentOptions = emptyList(),
            shopProperties = ShopProperties(isSafeDeal = true, isMarketplace = false)
        )))

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel)

        // assert
        assertThat(action, instanceOf(PaymentOptionList.Action.LoadPaymentOptionListFailed::class.java))
        action as PaymentOptionList.Action.LoadPaymentOptionListFailed
        assertThat(action.error, instanceOf(PaymentOptionListIsEmptyException::class.java))
    }
}