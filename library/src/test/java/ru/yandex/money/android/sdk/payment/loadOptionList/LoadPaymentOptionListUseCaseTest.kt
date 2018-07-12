/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.payment.loadOptionList

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
import ru.yandex.money.android.sdk.AbstractWallet
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.AuthorizedUser
import ru.yandex.money.android.sdk.CardBrand
import ru.yandex.money.android.sdk.Fee
import ru.yandex.money.android.sdk.GooglePay
import ru.yandex.money.android.sdk.LinkedCard
import ru.yandex.money.android.sdk.NewCard
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.SbolSmsInvoicing
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.YandexMoney
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.SaveLoadedPaymentOptionsListGateway
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class LoadPaymentOptionListUseCaseTest {

    private val testUser = AuthorizedUser("username")
    private val testInputModel = Amount(BigDecimal.TEN, RUB)
    private val testCharge = Amount(testInputModel.value + BigDecimal.ONE, RUB)
    private val testFee = Fee(
        service = Amount(BigDecimal.ONE, RUB),
        counterparty = Amount(BigDecimal("0.5"), RUB)
    )
    private val availableOptions = mutableListOf(
        NewCard(0, testCharge, testFee),
        Wallet(1, testCharge, testFee, "123456787654321", Amount(BigDecimal.TEN, RUB), "test user"),
        AbstractWallet(2, testCharge, testFee),
        LinkedCard(3, testCharge, testFee, "1234567887654321", CardBrand.VISA, "123456787654321"),
        LinkedCard(4, testCharge, testFee, "1234567887654321", CardBrand.VISA, "123456787654321", "test name"),
        SbolSmsInvoicing(5, testCharge, testFee),
        GooglePay(6, testCharge, testFee)
    )

    private val restrictions = mutableSetOf<PaymentMethodType>()
    @Mock
    private lateinit var paymentOptionListGateway: PaymentOptionListGateway
    @Mock
    private lateinit var currentUserGateway: CurrentUserGateway
    @Mock
    private lateinit var saveLoadedPaymentOptionsListGateway: SaveLoadedPaymentOptionsListGateway
    private lateinit var useCase: LoadPaymentOptionListUseCase

    @Before
    fun setUp() {
        on(currentUserGateway.currentUser).thenReturn(testUser)
        on(paymentOptionListGateway.getPaymentOptions(testInputModel, testUser)).thenReturn(availableOptions)

        useCase = LoadPaymentOptionListUseCase(
            paymentOptionListRestrictions = restrictions,
            paymentOptionListGateway = paymentOptionListGateway,
            saveLoadedPaymentOptionsListGateway = saveLoadedPaymentOptionsListGateway,
            currentUserGateway = currentUserGateway
        )
    }

    @Test
    fun `should return payment option list when Wallet not present`() {
        // prepare
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(*availableOptions.toTypedArray()))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return Wallet when Wallet present and no LinkedCards`() {
        // prepare
        availableOptions.removeIf { it is LinkedCard }

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(instanceOf(Wallet::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
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
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(*availableOptions.toTypedArray()))

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
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(instanceOf(NewCard::class.java)))

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
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel.size, equalTo(1))
        assertThat(outputModel[0], instanceOf(GooglePay::class.java))

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
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(instanceOf(SbolSmsInvoicing::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is SbolSmsInvoicing })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return Wallet when yandex money restriction and no LinkedCards`() {
        // prepare
        availableOptions.removeIf { it is LinkedCard }
        restrictions.add(PaymentMethodType.YANDEX_MONEY)

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(instanceOf(Wallet::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YandexMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when yandex money restriction`() {
        // prepare
        restrictions.add(PaymentMethodType.YANDEX_MONEY)

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(
            outputModel,
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
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YandexMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when yandex money restriction and no Wallet`() {
        // prepare
        restrictions.add(PaymentMethodType.YANDEX_MONEY)
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(
            outputModel,
            contains(
                instanceOf(AbstractWallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java)
            )
        )

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions.filter { it is YandexMoney })
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when all restrictions set and no Wallet`() {
        // prepare
        restrictions.addAll(PaymentMethodType.values())
        availableOptions.removeIf { it is Wallet }

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(
            outputModel, contains(
                instanceOf(NewCard::class.java),
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
            verify(saveLoadedPaymentOptionsListGateway)
                .saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return Wallet when all restrictions set, Wallet present and no LinkedCards`() {
        // prepare
        availableOptions.removeIf { it is LinkedCard }
        restrictions.addAll(PaymentMethodType.values())

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(outputModel, contains(instanceOf(Wallet::class.java)))

        inOrder(paymentOptionListGateway, currentUserGateway, saveLoadedPaymentOptionsListGateway).apply {
            verify(currentUserGateway).currentUser
            verify(paymentOptionListGateway).getPaymentOptions(testInputModel, testUser)
            verify(saveLoadedPaymentOptionsListGateway).saveLoadedPaymentOptionsList(availableOptions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `should return payment option list when all restrictions set and Wallet present`() {
        // prepare
        restrictions.addAll(PaymentMethodType.values())

        // invoke
        val outputModel = useCase(testInputModel)

        // assert
        assertThat(
            outputModel,
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
        val noRestrictionsOutput = useCase(testInputModel)

        // prepare fullRestrictionsOutput
        restrictions.addAll(PaymentMethodType.values())

        // invoke fullRestrictionsOutput
        val fullRestrictionsOutput = useCase(testInputModel)

        // assert
        assertThat(noRestrictionsOutput, contains(*fullRestrictionsOutput.toTypedArray()))
    }

    @Test(expected = PaymentOptionListIsEmptyException::class)
    fun `should throw Exception when empty payment option list`() {
        // prepare
        on(paymentOptionListGateway.getPaymentOptions(testInputModel, testUser)).thenReturn(listOf())

        // invoke
        useCase(testInputModel)

        // assert that PaymentOptionListIsEmptyException thrown
    }
}