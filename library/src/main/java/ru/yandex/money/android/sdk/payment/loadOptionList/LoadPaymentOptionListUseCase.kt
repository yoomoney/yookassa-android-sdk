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

import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.GooglePay
import ru.yandex.money.android.sdk.LinkedCard
import ru.yandex.money.android.sdk.NewCard
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.SbolSmsInvoicing
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.YandexMoney
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.SaveLoadedPaymentOptionsListGateway

internal class LoadPaymentOptionListUseCase(
    private val paymentOptionListRestrictions: Set<PaymentMethodType>,
    private val paymentOptionListGateway: PaymentOptionListGateway,
    private val saveLoadedPaymentOptionsListGateway: SaveLoadedPaymentOptionsListGateway,
    private val currentUserGateway: CurrentUserGateway
) : UseCase<PaymentOptionListInputModel, PaymentOptionListOutputModel> {

    override fun invoke(inputModel: PaymentOptionListInputModel): PaymentOptionListOutputModel {
        val currentUser = currentUserGateway.currentUser
        val paymentOptions = paymentOptionListGateway.getPaymentOptions(inputModel, currentUser)
        val options = when {
            paymentOptionListRestrictions.isEmpty() -> paymentOptions
            else -> paymentOptions.filter { it.toAllowed() in paymentOptionListRestrictions }
        }
        saveLoadedPaymentOptionsListGateway.saveLoadedPaymentOptionsList(options)
        return options
            .takeIf { it.none { it is LinkedCard } }?.filter { it is Wallet }?.takeUnless(List<PaymentOption>::isEmpty)
                ?: options.takeUnless(List<PaymentOption>::isEmpty)
                ?: throw PaymentOptionListIsEmptyException()
    }
}

internal typealias PaymentOptionListInputModel = Amount

internal typealias PaymentOptionListOutputModel = List<PaymentOption>

private fun PaymentOption.toAllowed() = when (this) {
    is NewCard -> PaymentMethodType.BANK_CARD
    is YandexMoney -> PaymentMethodType.YANDEX_MONEY
    is SbolSmsInvoicing -> PaymentMethodType.SBERBANK
    is GooglePay -> PaymentMethodType.GOOGLE_PAY
}
