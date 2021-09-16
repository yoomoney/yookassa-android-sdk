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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.AnonymousUser
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import java.math.BigDecimal

class MockPaymentOptionListRepositoryTest {

    @Test
    fun testNoLinkedCards() {
        // prepare
        val gateway = MockPaymentOptionListRepository(0, null)

        // invoke
        val optionsResult = gateway.getPaymentOptions(
            Amount(BigDecimal.TEN, RUB),
            AuthorizedUser()
        )

        assertThat(optionsResult, instanceOf(Result.Success::class.java))
        val options = (optionsResult as Result.Success).value.paymentOptions

        // assert
        assertThat(
            options,
            contains(
                instanceOf(Wallet::class.java),
                instanceOf(SberBank::class.java),
                instanceOf(GooglePay::class.java),
                instanceOf(BankCardPaymentOption::class.java)
            )
        )
    }

    @Test
    fun testWithLinkedCards() {
        // prepare
        val gateway = MockPaymentOptionListRepository(3, null)
        // invoke

        val optionsResult = gateway.getPaymentOptions(
            Amount(BigDecimal.TEN, RUB),
            AuthorizedUser()
        )

        assertThat(optionsResult, instanceOf(Result.Success::class.java))
        val options = (optionsResult as Result.Success).value.paymentOptions

        // assert
        assertThat(
            options,
            contains(
                instanceOf(Wallet::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(LinkedCard::class.java),
                instanceOf(SberBank::class.java),
                instanceOf(GooglePay::class.java),
                instanceOf(BankCardPaymentOption::class.java)
            )
        )
    }

    @Test
    fun testAnonymousUser() {
        // prepare
        val gateway = MockPaymentOptionListRepository(3, null)
        // invoke
        val optionsResult = gateway.getPaymentOptions(
            Amount(BigDecimal.TEN, RUB),
            AnonymousUser
        )

        assertThat(optionsResult, instanceOf(Result.Success::class.java))
        val options = (optionsResult as Result.Success).value.paymentOptions

        // assert
        assertThat(
            options,
            contains(
                instanceOf(AbstractWallet::class.java),
                instanceOf(SberBank::class.java),
                instanceOf(GooglePay::class.java),
                instanceOf(BankCardPaymentOption::class.java)
            )
        )
    }
}
