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

package ru.yoomoney.sdk.kassa.payments.payment.tokenize

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo

internal sealed class TokenizeInputModel: Parcelable {
    abstract val paymentOptionId: Int
    abstract val allowWalletLinking: Boolean
    abstract val instrumentBankCard: PaymentInstrumentBankCard?
}

@Parcelize
internal data class TokenizePaymentOptionInputModel(
    override val paymentOptionId: Int,
    val savePaymentMethod: Boolean,
    val savePaymentInstrument: Boolean,
    val confirmation: Confirmation,
    val paymentOptionInfo: PaymentOptionInfo? = null,
    override val allowWalletLinking: Boolean,
    override val instrumentBankCard: PaymentInstrumentBankCard? = null
): TokenizeInputModel(), Parcelable

@Parcelize
internal data class TokenizeInstrumentInputModel(
    override val paymentOptionId: Int,
    val savePaymentMethod: Boolean,
    override val instrumentBankCard: PaymentInstrumentBankCard,
    override val allowWalletLinking: Boolean = false,
    val confirmation: Confirmation,
    val csc: String? = null
): TokenizeInputModel(), Parcelable

internal data class TokenizeOutputModel(
    val token: String,
    val option: PaymentOption,
    val instrumentBankCard: PaymentInstrumentBankCard?
)