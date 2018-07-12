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

package ru.yandex.money.android.sdk.impl.paymentOptionList

import ru.yandex.money.android.sdk.AbstractWallet
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.AnonymousUser
import ru.yandex.money.android.sdk.AuthorizedUser
import ru.yandex.money.android.sdk.CardBrand
import ru.yandex.money.android.sdk.CurrentUser
import ru.yandex.money.android.sdk.Fee
import ru.yandex.money.android.sdk.GooglePay
import ru.yandex.money.android.sdk.LinkedCard
import ru.yandex.money.android.sdk.NewCard
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.SbolSmsInvoicing
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListGateway
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.util.Random

internal class MockPaymentOptionListGateway(
    private val linkedCardsCount: Int
) : PaymentOptionListGateway {

    private val random = Random()

    override fun getPaymentOptions(amount: Amount, currentUser: CurrentUser): List<PaymentOption> {
        sleep(1000L)
        val fee = Fee(Amount(BigDecimal.ONE, amount.currency), Amount(BigDecimal("0.5"), amount.currency))
        val id = generateSequence(0, 1::plus).iterator()
        return when (currentUser) {
            is AuthorizedUser -> createAuthorizedList(id, amount, fee, currentUser)
            AnonymousUser -> createAnonymousList(id, amount, fee)
        }
    }

    private fun createAuthorizedList(
        id: Iterator<Int>,
        amount: Amount,
        fee: Fee,
        currentUser: AuthorizedUser
    ) = mutableListOf<PaymentOption>().apply {
        add(
            Wallet(
                id = id.next(),
                charge = amount,
                fee = fee,
                walletId = "11234567887654321",
                balance = Amount(BigDecimal.TEN, amount.currency),
                userName = currentUser.userName
            )
        )
        addAll(generateLinkedCards(id, amount, fee).take(linkedCardsCount))
        add(SbolSmsInvoicing(id.next(), amount, fee))
        add(GooglePay(id.next(), amount, fee))
        add(NewCard(id.next(), amount, fee))
    }.toList()

    private fun createAnonymousList(
        id: Iterator<Int>,
        amount: Amount,
        fee: Fee
    ) = listOf(
        AbstractWallet(
            id = id.next(),
            charge = Amount(amount.value, amount.currency),
            fee = fee
        ),
        SbolSmsInvoicing(id.next(), Amount(amount.value, amount.currency), fee),
        GooglePay(id.next(), amount, fee),
        NewCard(id.next(), Amount(amount.value, amount.currency), fee)
    )

    private fun generateLinkedCards(
        id: Iterator<Int>,
        charge: Amount,
        fee: Fee?
    ) = generateSequence {
        val cardId = generateSequence { random.nextInt(10) }.take(16).joinToString("")
        LinkedCard(
            id = id.next(),
            charge = charge,
            fee = fee,
            cardId = cardId,
            brand = randomCardBrand(),
            pan = cardId.replaceRange(4, 12, "*".repeat(8)),
            name = "testCardName".takeIf { random.nextInt(10) < 5 }
        )
    }

    private fun randomCardBrand() = CardBrand.values()[random.nextInt(CardBrand.values().size)]
}
