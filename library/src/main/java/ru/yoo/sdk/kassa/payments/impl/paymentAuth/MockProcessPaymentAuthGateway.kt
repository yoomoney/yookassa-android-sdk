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

package ru.yoo.sdk.kassa.payments.impl.paymentAuth

import ru.yoo.sdk.kassa.payments.impl.ApiMethodException
import ru.yoo.sdk.kassa.payments.model.CurrentUser
import ru.yoo.sdk.kassa.payments.model.ErrorCode
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthToken
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthWrongAnswer
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthGateway
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthGatewayResponse
import java.lang.Thread.sleep

internal class MockProcessPaymentAuthGateway :
    ProcessPaymentAuthGateway {
    override fun getPaymentAuthToken(currentUser: CurrentUser, passphrase: String): ProcessPaymentAuthGatewayResponse {
        sleep(1000L)
        return when (passphrase) {
            "fail" -> PaymentAuthWrongAnswer()
            "tech" -> throw ApiMethodException(ErrorCode.TECHNICAL_ERROR)
            else -> PaymentAuthToken(currentUser.toString() + passphrase)
        }
    }

    override fun getPaymentAuthToken(currentUser: CurrentUser): ProcessPaymentAuthGatewayResponse {
        sleep(1000L)
        return PaymentAuthToken(currentUser.toString())
    }
}
