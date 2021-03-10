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

package ru.yoo.sdk.kassa.payments.payment.loadOptionList

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
import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoo.sdk.kassa.payments.extensions.RUB
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListNoWalletOutputModel
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListSuccessOutputModel
import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.CardBrand
import ru.yoo.sdk.kassa.payments.model.CardInfo
import ru.yoo.sdk.kassa.payments.model.Fee
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.Result
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.Wallet
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoo.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoo.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoo.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCaseImpl
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
        NewCard(0, testCharge, testFee, true),
        Wallet(
            1, testCharge, testFee, "123456787654321",
            Amount(BigDecimal.TEN, RUB), true
        ),
        AbstractWallet(2, testCharge, testFee, false),
        LinkedCard(
            3,
            testCharge,
            testFee,
            "1234567887654321",
            CardBrand.VISA,
            "123456787654321",
            null,
            false,
            true
        ),
        LinkedCard(
            4,
            testCharge,
            testFee,
            "1234567887654321",
            CardBrand.VISA,
            "123456787654321",
            "test name",
            false,
            true
        ),
        SbolSmsInvoicing(5, testCharge, testFee, false),
        GooglePay(6, testCharge, testFee, false)
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
        ).thenReturn(Result.Success(availableOptions))

        on(googlePayRepository.checkGooglePayAvailable()).thenReturn(true)

        useCase = PaymentOptionsListUseCaseImpl(
            paymentOptionListRestrictions = restrictions,
            paymentOptionListRepository = paymentOptionListRepository,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            saveLoadedPaymentOptionsListRepository = saveLoadedPaymentOptionsListRepository,
            currentUserRepository = currentUserRepository,
            googlePayRepository = googlePayRepository,
            paymentOptionRepository = mock(),
            loadedPaymentOptionListRepository = mock()
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
                paymentMethodId = testPaymentMethodId,
                first = "123456",
                last = "7890",
                expiryYear = "20",
                expiryMonth = "10",
                savePaymentMethodAllowed = true
            )
        )

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel, testPaymentMethodId) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val options = (action.content as PaymentOptionListSuccessOutputModel).options

        // assert
        assertThat(options, contains(instanceOf(PaymentIdCscConfirmation::class.java)))

        val captor = argumentCaptor<List<PaymentOption>>()

        inOrder(paymentOptionListRepository, currentUserRepository, paymentMethodInfoGateway, saveLoadedPaymentOptionsListRepository).apply {
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

        inOrder(paymentOptionListRepository, paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
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
        assertThat(options, contains(instanceOf(NewCard::class.java)))

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
        assertThat(options, contains(instanceOf(SbolSmsInvoicing::class.java)))

        inOrder(paymentOptionListRepository, currentUserRepository, saveLoadedPaymentOptionsListRepository).apply {
            verify(currentUserRepository).currentUser
            verify(paymentOptionListRepository).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListRepository)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is SbolSmsInvoicing })
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
                instanceOf(NewCard::class.java),
                instanceOf(Wallet::class.java),
                instanceOf(AbstractWallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(SbolSmsInvoicing::class.java),
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
        val noRestrictionsAction = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
        val noRestrictionsOptions = (noRestrictionsAction.content as PaymentOptionListSuccessOutputModel).options
        // prepare fullRestrictionsOutput
        restrictions.addAll(PaymentMethodType.values())

        // invoke fullRestrictionsOutput
        val fullRestrictionsAction = useCase.loadPaymentOptions(testInputModel) as PaymentOptionList.Action.LoadPaymentOptionListSuccess
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
        ).thenReturn(Result.Success(listOf()))

        // invoke
        val action = useCase.loadPaymentOptions(testInputModel)

        // assert
        assertThat(action, instanceOf(PaymentOptionList.Action.LoadPaymentOptionListFailed::class.java))
        action as PaymentOptionList.Action.LoadPaymentOptionListFailed
        assertThat(action.error, instanceOf(PaymentOptionListIsEmptyException::class.java))
    }
}