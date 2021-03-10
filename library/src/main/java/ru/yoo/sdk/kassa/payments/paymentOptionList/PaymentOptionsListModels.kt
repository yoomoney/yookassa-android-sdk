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

package ru.yoo.sdk.kassa.payments.paymentOptionList

import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.YooMoney


internal sealed class PaymentOptionListOutputModel(open val options: List<PaymentOption>)

internal data class PaymentOptionListSuccessOutputModel(
    override val options: List<PaymentOption>
): PaymentOptionListOutputModel(options)

internal data class PaymentOptionListNoWalletOutputModel(
    override val options: List<PaymentOption>
) : PaymentOptionListOutputModel(options)

private fun PaymentOption.toAllowed() = when (this) {
    is NewCard -> PaymentMethodType.BANK_CARD
    is YooMoney -> PaymentMethodType.YOO_MONEY
    is SbolSmsInvoicing -> PaymentMethodType.SBERBANK
    is GooglePay -> PaymentMethodType.GOOGLE_PAY
    is PaymentIdCscConfirmation -> PaymentMethodType.BANK_CARD
}