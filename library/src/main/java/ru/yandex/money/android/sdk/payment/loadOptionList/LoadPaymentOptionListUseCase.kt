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
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.model.GooglePay
import ru.yandex.money.android.sdk.model.NewCard
import ru.yandex.money.android.sdk.model.PaymentIdCscConfirmation
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.SbolSmsInvoicing
import ru.yandex.money.android.sdk.model.UseCase
import ru.yandex.money.android.sdk.model.Wallet
import ru.yandex.money.android.sdk.model.YandexMoney
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.SaveLoadedPaymentOptionsListGateway
import ru.yandex.money.android.sdk.payment.loadPaymentInfo.PaymentMethodInfoGateway

internal class LoadPaymentOptionListUseCase(
    private val paymentOptionListRestrictions: Set<PaymentMethodType>,
    private val paymentOptionListGateway: PaymentOptionListGateway,
    private val saveLoadedPaymentOptionsListGateway: SaveLoadedPaymentOptionsListGateway,
    private val paymentMethodInfoGateway: PaymentMethodInfoGateway,
    private val currentUserGateway: CurrentUserGateway
) : UseCase<PaymentOptionListInputModel, PaymentOptionListOutputModel> {

    override fun invoke(inputModel: PaymentOptionListInputModel): PaymentOptionListOutputModel {
        val currentUser = currentUserGateway.currentUser

        var paymentOptions = paymentOptionListGateway.getPaymentOptions(inputModel.amount, currentUser)
        if (inputModel is PaymentOptionPaymentMethodInputModel) {
            val paymentMethodInfo = paymentMethodInfoGateway.getPaymentMethodInfo(inputModel.paymentMethodId)
            val paymentOption = when(paymentMethodInfo.type) {
                PaymentMethodType.BANK_CARD -> paymentOptions.find { it is NewCard }
                else -> null
            }
            if (paymentOption != null && paymentMethodInfo.card != null) {
                paymentOptions = listOf(
                    PaymentIdCscConfirmation(
                        id = paymentOption.id,
                        charge = paymentOption.charge,
                        fee = paymentOption.fee,
                        paymentMethodId = inputModel.paymentMethodId,
                        first = paymentMethodInfo.card.first,
                        last = paymentMethodInfo.card.last,
                        expiryMonth = paymentMethodInfo.card.expiryMonth,
                        expiryYear = paymentMethodInfo.card.expiryYear,
                        savePaymentMethodAllowed = paymentOption.savePaymentMethodAllowed
                    )
                )
            } else {
                throw PaymentOptionListIsEmptyException()
            }
        }
        val options = when {
            paymentOptionListRestrictions.isEmpty() -> paymentOptions
            else -> {
                val hasYooMoney = paymentOptions.find { it is YandexMoney && it.paymentMethodType == PaymentMethodType.YOO_MONEY } != null
                val paymentOptionListRestrictions = if (hasYooMoney && paymentOptionListRestrictions.contains(PaymentMethodType.YANDEX_MONEY)) {
                        paymentOptionListRestrictions.toMutableSet().apply {
                            remove(PaymentMethodType.YANDEX_MONEY)
                            add(PaymentMethodType.YOO_MONEY)
                        }
                } else {
                    paymentOptionListRestrictions
                }
                paymentOptions.filter { it.toAllowed() in paymentOptionListRestrictions }
            }
        }
        saveLoadedPaymentOptionsListGateway.saveLoadedPaymentOptionsList(options)

        return options.takeUnless(List<PaymentOption>::isEmpty)?.let { paymentOptions ->
            if (currentUser is AuthorizedUser
                && paymentOptionListRestrictions.any { it == PaymentMethodType.YANDEX_MONEY || it == PaymentMethodType.YOO_MONEY }
                && paymentOptions.filterIsInstance<Wallet>().isEmpty()) {
                PaymentOptionListNoWalletOutputModel
            } else {
                PaymentOptionListSuccessOutputModel(paymentOptions)
            }
        }
            ?: throw PaymentOptionListIsEmptyException()
    }
}

internal sealed class PaymentOptionListInputModel {
    abstract val amount: Amount
}

internal data class PaymentOptionAmountInputModel(
    override val amount: Amount
) : PaymentOptionListInputModel()

internal data class PaymentOptionPaymentMethodInputModel(
    override val amount: Amount,
    val paymentMethodId: String
) : PaymentOptionListInputModel()


internal sealed class PaymentOptionListOutputModel

internal data class PaymentOptionListSuccessOutputModel(val options: List<PaymentOption>): PaymentOptionListOutputModel()

internal object PaymentOptionListNoWalletOutputModel : PaymentOptionListOutputModel()


private fun PaymentOption.toAllowed() = when (this) {
    is NewCard -> PaymentMethodType.BANK_CARD
    is YandexMoney -> paymentMethodType
    is SbolSmsInvoicing -> PaymentMethodType.SBERBANK
    is GooglePay -> PaymentMethodType.GOOGLE_PAY
    is PaymentIdCscConfirmation -> PaymentMethodType.BANK_CARD
}
