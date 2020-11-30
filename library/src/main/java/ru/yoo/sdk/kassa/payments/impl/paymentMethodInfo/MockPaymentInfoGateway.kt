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

package ru.yoo.sdk.kassa.payments.impl.paymentMethodInfo

import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.model.CardBrand
import ru.yoo.sdk.kassa.payments.model.CardInfo
import ru.yoo.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoo.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway

internal class MockPaymentInfoGateway() : PaymentMethodInfoGateway {
    override fun getPaymentMethodInfo(paymentMethodId: String): PaymentMethodBankCard {
        Thread.sleep(1000L)
        return PaymentMethodBankCard(
            type = PaymentMethodType.BANK_CARD,
            id = "11234567887654321",
            saved = true,
            cscRequired = true,
            title = "11234567887654321",
            card = CardInfo(
                first = "123456",
                last = "1234",
                expiryYear = "2020",
                expiryMonth = "12",
                cardType = CardBrand.MASTER_CARD,
                source = PaymentMethodType.GOOGLE_PAY
            )
        )
    }
}