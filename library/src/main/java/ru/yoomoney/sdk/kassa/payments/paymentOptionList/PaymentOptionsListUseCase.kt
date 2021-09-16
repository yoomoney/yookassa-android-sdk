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

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodRepository
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListIsEmptyException
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList.Action

internal interface PaymentOptionsListUseCase {
    val isPaymentOptionsActual: Boolean
    suspend fun loadPaymentOptions(amount: Amount, paymentMethodId: String? = null): Action
    fun selectPaymentOption(paymentOptionId: Int, instrumentId: String?): PaymentOption?
}

internal class PaymentOptionsListUseCaseImpl(
    private val paymentOptionListRestrictions: Set<PaymentMethodType>,
    private val paymentOptionListRepository: PaymentOptionListRepository,
    private val saveLoadedPaymentOptionsListRepository: SaveLoadedPaymentOptionsListRepository,
    private val paymentMethodInfoGateway: PaymentMethodInfoGateway,
    private val currentUserRepository: CurrentUserRepository,
    private val googlePayRepository: GooglePayRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val shopPropertiesRepository: ShopPropertiesRepository
) : PaymentOptionsListUseCase {

    override val isPaymentOptionsActual: Boolean
        get() = loadedPaymentOptionListRepository.isActual

    private val googlePayAvailable by lazy { googlePayRepository.checkGooglePayAvailable() }

    override suspend fun loadPaymentOptions(amount: Amount, paymentMethodId: String?): Action {
        loadedPaymentOptionListRepository
            .takeIf { isPaymentOptionsActual  }
            ?.getLoadedPaymentOptions()
            ?.let { return Action.LoadPaymentOptionListSuccess(PaymentOptionListSuccessOutputModel(it)) }

        val currentUser = currentUserRepository.currentUser

        var paymentOptions: List<PaymentOption>
        when (val paymentOptionsResponse = paymentOptionListRepository.getPaymentOptions(amount, currentUser)) {
            is Result.Success -> {
                shopPropertiesRepository.shopProperties = paymentOptionsResponse.value.shopProperties
                paymentOptions = paymentOptionsResponse.value.paymentOptions
            }
            is Result.Fail -> return Action.LoadPaymentOptionListFailed(paymentOptionsResponse.value)
        }

        paymentOptions = paymentOptions.takeIf { googlePayAvailable } ?: paymentOptions.filterNot { it is GooglePay }
        if (paymentMethodId != null) {
            when(val response = paymentMethodInfoGateway.getPaymentMethodInfo(paymentMethodId)) {
                is Result.Success -> {
                    val paymentOption = when (response.value.type) {
                        PaymentMethodType.BANK_CARD -> paymentOptions.find { it is BankCardPaymentOption }
                        else -> null
                    }
                    if (paymentOption != null && response.value.card != null) {
                        paymentOptions = listOf(
                            PaymentIdCscConfirmation(
                                id = paymentOption.id,
                                charge = paymentOption.charge,
                                fee = paymentOption.fee,
                                paymentMethodId = paymentMethodId,
                                first = response.value.card.first,
                                last = response.value.card.last,
                                brand = response.value.card.cardType,
                                expiryMonth = response.value.card.expiryMonth,
                                expiryYear = response.value.card.expiryYear,
                                savePaymentMethodAllowed = paymentOption.savePaymentMethodAllowed,
                                confirmationTypes = paymentOption.confirmationTypes,
                                savePaymentInstrument = paymentOption.savePaymentInstrument
                            )
                        )
                    } else {
                        return Action.LoadPaymentOptionListFailed(PaymentOptionListIsEmptyException())
                    }
                }
                is Result.Fail -> return Action.LoadPaymentOptionListFailed(response.value)
            }
        }
        val options = when {
            paymentOptionListRestrictions.isEmpty() -> paymentOptions
            else -> paymentOptions.filter { it.toAllowed() in paymentOptionListRestrictions }
        }
        saveLoadedPaymentOptionsListRepository.saveLoadedPaymentOptionsList(options)
        return options.takeUnless(List<PaymentOption>::isEmpty)?.let { paymentOptions ->
            if (currentUser is AuthorizedUser
                && paymentOptionListRestrictions.any { it == PaymentMethodType.YOO_MONEY }
                && paymentOptions.filterIsInstance<Wallet>().isEmpty()
            ) {
                Action.LoadPaymentOptionListSuccess(PaymentOptionListNoWalletOutputModel(paymentOptions))
            } else {
                loadedPaymentOptionListRepository.isActual = true
                Action.LoadPaymentOptionListSuccess(PaymentOptionListSuccessOutputModel(paymentOptions))
            }
        } ?: Action.LoadPaymentOptionListFailed(PaymentOptionListIsEmptyException())
    }

    override fun selectPaymentOption(paymentOptionId: Int, instrumentId: String?): PaymentOption? {
        paymentMethodRepository.paymentOptionId = paymentOptionId
        paymentMethodRepository.instrumentId = instrumentId
        return loadedPaymentOptionListRepository
            .getLoadedPaymentOptions()
            .find { it.id == paymentOptionId }
    }
}

private fun PaymentOption.toAllowed() = when (this) {
    is BankCardPaymentOption -> PaymentMethodType.BANK_CARD
    is YooMoney -> PaymentMethodType.YOO_MONEY
    is SberBank -> PaymentMethodType.SBERBANK
    is GooglePay -> PaymentMethodType.GOOGLE_PAY
    is PaymentIdCscConfirmation -> PaymentMethodType.BANK_CARD
}