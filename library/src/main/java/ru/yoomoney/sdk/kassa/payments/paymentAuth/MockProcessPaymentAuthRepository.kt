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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import java.lang.Thread.sleep

internal class MockProcessPaymentAuthRepository :
    ProcessPaymentAuthRepository {
    override fun getPaymentAuthToken(currentUser: CurrentUser, passphrase: String): Result<ProcessPaymentAuthGatewayResponse> {
        sleep(1000L)
        return when (passphrase) {
            "fail" -> Result.Success(PaymentAuthWrongAnswer(AuthTypeState.SMS(60, 4, 3, 3)))
            "tech" -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
            else -> Result.Success(PaymentAuthToken(currentUser.toString() + passphrase))
        }
    }

    override fun getPaymentAuthToken(currentUser: CurrentUser): Result<ProcessPaymentAuthGatewayResponse> {
        sleep(1000L)
        return Result.Success(PaymentAuthToken(currentUser.toString()))
    }
}
