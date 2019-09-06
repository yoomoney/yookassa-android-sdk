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

package ru.yandex.money.android.sdk.payment.changeOption

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.mock
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.createAmount
import ru.yandex.money.android.sdk.impl.paymentMethodInfo.MockPaymentInfoGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.MockPaymentOptionListGateway
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.InMemoryPaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.loadOptionList.LoadPaymentOptionListUseCase
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionAmountInputModel

class ChangePaymentOptionIntegrationTest {

    private val userName = "test name"
    private val paymentOptionsGateway = MockPaymentOptionListGateway(3, null)
    private val currentUserGateway = mock(CurrentUserGateway::class.java).apply {
        on(currentUser).thenReturn(AuthorizedUser(userName))
    }
    private val paymentMethodInfoGateway = MockPaymentInfoGateway()
    private val restrictions = mutableSetOf<PaymentMethodType>()
    private val loadUseCase = LoadPaymentOptionListUseCase(
        paymentOptionListRestrictions = restrictions,
        paymentOptionListGateway = paymentOptionsGateway,
        paymentMethodInfoGateway = paymentMethodInfoGateway,
        saveLoadedPaymentOptionsListGateway = InMemoryPaymentOptionListGateway,
        currentUserGateway = currentUserGateway
    )
    private val changeUseCase = ChangePaymentOptionUseCase(InMemoryPaymentOptionListGateway)

    @Test
    fun `mock change payment option no restrictions`() {
        // prepare

        // invoke
        val paymentOptions = loadUseCase(PaymentOptionAmountInputModel(createAmount()))
        val paymentOptionsAfterChange = changeUseCase(ChangePaymentOptionInputModel)

        // assert
        assertThat(paymentOptions, equalTo(paymentOptionsAfterChange))
    }

    @Test
    fun `mock change payment option with restrictions`() {
        // prepare
        restrictions.add(PaymentMethodType.YANDEX_MONEY)

        // invoke
        val paymentOptions = loadUseCase(PaymentOptionAmountInputModel(createAmount()))
        val paymentOptionsAfterChange = changeUseCase(ChangePaymentOptionInputModel)

        // assert
        assertThat(paymentOptions, equalTo(paymentOptionsAfterChange))
    }
}
