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
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.impl.extensions.RUB
import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.CardBrand
import ru.yoo.sdk.kassa.payments.model.CardInfo
import ru.yoo.sdk.kassa.payments.model.Fee
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.Wallet
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.CurrentUserGateway
import ru.yoo.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListGateway
import ru.yoo.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class LoadPaymentOptionListUseCaseTest {

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
    private lateinit var paymentOptionListGateway: PaymentOptionListGateway
    @Mock
    private lateinit var currentUserGateway: CurrentUserGateway
    @Mock
    private lateinit var saveLoadedPaymentOptionsListGateway: SaveLoadedPaymentOptionsListGateway
    @Mock
    private lateinit var paymentMethodInfoGateway: PaymentMethodInfoGateway
    private lateinit var useCase: LoadPaymentOptionListUseCase

    @Before
    fun setUp() {
        on(
            paymentMethodInfoGateway.getPaymentMethodInfo(
                testPaymentMethodId
            )
        ).thenReturn(bankCardInfo)
        on(currentUserGateway.currentUser).thenReturn(testUser)
        on(
            paymentOptionListGateway.getPaymentOptions(
                testInputModel,
                testUser
            )
        ).thenReturn(availableOptions)

        useCase = LoadPaymentOptionListUseCase(
            paymentOptionListRestrictions = restrictions,
            paymentOptionListGateway = paymentOptionListGateway,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            saveLoadedPaymentOptionsListGateway = saveLoadedPaymentOptionsListGateway,
            currentUserGateway = currentUserGateway
        )
    }

    @Test
    fun `should return SavedLinkedCard when paymentMethodId is set`() {
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
        val outputModel = useCase(PaymentOptionPaymentMethodInputModel(testInputModel, testPaymentMethodId)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options, contains(instanceOf(PaymentIdCscConfirmation::class.java)))

        val captor = argumentCaptor<List<PaymentOption>>()

        inOrder(paymentOptionListGateway, currentUserGateway, paymentMethodInfoGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(paymentMethodInfoGateway).getPaymentMethodInfo(testPaymentMethodId)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(captor.capture())
            verifyNoMoreInteractions()
        }

        assertThat(captor.firstValue, equalTo(savedLinkedCard))
    }

    @Test
    fun `should return payment option list when Wallet not present`() {
        // prepare
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options, contains(*availableOptions.toTypedArray()))

        inOrder(paymentOptionListGateway, paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when Wallet present`() {
        // prepare

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options, contains(*availableOptions.toTypedArray()))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return NewCard when bank card restriction`() {
        // prepare
        restrictions.add(PaymentMethodType.BANK_CARD)

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options, contains(instanceOf(NewCard::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return GooglePay when google pay restriction`() {
        // prepare
        restrictions.add(PaymentMethodType.GOOGLE_PAY)

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options.size, equalTo(1))
        assertThat(outputModel.options[0], instanceOf(GooglePay::class.java))

        inOrder(paymentOptionListGateway, currentUserGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return SbolSmsInvoicing when sberbank restriction`() {
        // prepare
        restrictions.add(PaymentMethodType.SBERBANK)

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(outputModel.options, contains(instanceOf(SbolSmsInvoicing::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is SbolSmsInvoicing })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when yoo money restriction`() {
        // prepare
        restrictions.add(PaymentMethodType.YOO_MONEY)

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(
            outputModel.options,
            contains(
                instanceOf(Wallet::class.java),
                instanceOf(AbstractWallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java)
            )
        )

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YooMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should show no wallet message when yoo money restriction and no Wallet`() {
        // prepare
        restrictions.add(PaymentMethodType.YOO_MONEY)
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel))

        // assert
        assertThat(outputModel, instanceOf(PaymentOptionListNoWalletOutputModel::class.java))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YooMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should show no wallet message when all restrictions set and no Wallet`() {
        // prepare
        restrictions.addAll(PaymentMethodType.values())
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel))

        // assert
        assertThat(outputModel, instanceOf(PaymentOptionListNoWalletOutputModel::class.java))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when all restrictions set and Wallet present`() {
        // prepare
        restrictions.addAll(PaymentMethodType.values())

        // invoke
        val outputModel = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(
            outputModel.options,
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

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `test all restrictions enabled equal to no restriction set`() {
        // prepare noRestrictionsOutput

        // invoke noRestrictionsOutput
        val noRestrictionsOutput = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // prepare fullRestrictionsOutput
        restrictions.addAll(PaymentMethodType.values())

        // invoke fullRestrictionsOutput
        val fullRestrictionsOutput = useCase(PaymentOptionAmountInputModel(testInputModel)) as PaymentOptionListSuccessOutputModel

        // assert
        assertThat(noRestrictionsOutput.options, contains(*fullRestrictionsOutput.options.toTypedArray()))
    }

    @Test(expected = PaymentOptionListIsEmptyException::class)
    fun `should throw Exception when empty payment option list`() {
        // prepare
        on(
            paymentOptionListGateway.getPaymentOptions(
                testInputModel,
                testUser
            )
        ).thenReturn(listOf())

        // invoke
        useCase(PaymentOptionAmountInputModel(testInputModel))

        // assert that PaymentOptionListIsEmptyException thrown
    }
}