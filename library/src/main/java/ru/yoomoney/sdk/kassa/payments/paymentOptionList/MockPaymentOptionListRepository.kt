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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.AnonymousUser
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.NewCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.util.Random
import ru.yoomoney.sdk.kassa.payments.model.Result

internal class MockPaymentOptionListRepository(
    private val linkedCardsCount: Int,
    private val fee: Fee?
) : PaymentOptionListRepository {

    private val random = Random()

    override fun getPaymentOptions(amount: Amount, currentUser: CurrentUser): Result<List<PaymentOption>> {
        sleep(1000L)
        val id = generateSequence(0, 1::plus).iterator()
        val charge = Amount(
            amount.value + (fee?.service?.value ?: BigDecimal.ZERO),
            amount.currency
        )
        return when (currentUser) {
            is AuthorizedUser -> createAuthorizedList(id, charge, fee, currentUser)
            AnonymousUser -> createAnonymousList(id, charge, fee)
        }
    }

    private fun createAuthorizedList(
        id: Iterator<Int>,
        amount: Amount,
        fee: Fee?,
        currentUser: AuthorizedUser
    ) = Result.Success(mutableListOf<PaymentOption>().apply {
        add(
            Wallet(
                id = id.next(),
                charge = amount,
                fee = fee,
                walletId = "11234567887654321",
                balance = Amount(
                    BigDecimal.TEN,
                    amount.currency
                ),
                savePaymentMethodAllowed = true,
                confirmationTypes = listOf(ConfirmationType.REDIRECT)
            )
        )
        addAll(generateLinkedCards(id, amount, fee).take(linkedCardsCount))
        add(SberBank(id.next(), amount, fee, false, listOf(ConfirmationType.REDIRECT, ConfirmationType.EXTERNAL)))
        add(GooglePay(id.next(), amount, fee, false, emptyList()))
        add(NewCard(id.next(), amount, fee, true, listOf(ConfirmationType.REDIRECT)))
    }.toList())

    private fun createAnonymousList(
        id: Iterator<Int>,
        amount: Amount,
        fee: Fee?
    ) = Result.Success(listOf(
        AbstractWallet(
            id = id.next(),
            charge = Amount(amount.value, amount.currency),
            fee = fee,
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        ),
        SberBank(
            id.next(),
            Amount(amount.value, amount.currency), fee,
            savePaymentMethodAllowed = false,
            confirmationTypes = listOf(ConfirmationType.REDIRECT, ConfirmationType.EXTERNAL)
        ),
        GooglePay(id.next(), amount, fee, false, emptyList()),
        NewCard(
            id.next(),
            Amount(amount.value, amount.currency),
            fee,
            true,
            listOf(ConfirmationType.REDIRECT)
        )
    ))

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
            name = "testCardName".takeIf { random.nextInt(10) < 5 },
            savePaymentMethodAllowed = true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        )
    }

    private fun randomCardBrand() = CardBrand.values()[random.nextInt(
        CardBrand.values().size)]
}
