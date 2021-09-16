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

package ru.yoomoney.sdk.kassa.payments.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import java.io.Serializable

internal sealed class Screen {

    data class PaymentOptions(val moneyAuthResult: MoneyAuth.Result? = null): Screen()

    object Contract: Screen()

    data class Tokenize(val tokenizeInputModel: TokenizeInputModel): Screen() {
        enum class TokenizeResult: Serializable {
            CANCEL
        }
    }

    data class TokenizeSuccessful(val tokenOutputModel: TokenizeOutputModel): Screen()
    object TokenizeCancelled: Screen()
    object MoneyAuth: Screen() {
        enum class Result: Serializable {
            SUCCESS,
            CANCEL
        }
    }
    data class PaymentAuth(val amount: Amount, val linkWalletToApp: Boolean): Screen() {
        enum class PaymentAuthResult: Serializable {
            SUCCESS,
            CANCEL
        }
    }
    data class UnbindLinkedCard(val paymentOption: PaymentOption): Screen()
    data class UnbindInstrument(val instrumentBankCard: PaymentInstrumentBankCard): Screen() {
        @Parcelize
        data class Success(val panUnbindingCard: String): Parcelable
    }
}