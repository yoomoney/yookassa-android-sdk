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

package ru.yoo.sdk.kassa.payments.checkoutParameters

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Wrapper for payment parameters. This class is used only in [Checkout.createSavedCardTokenizeIntent].
 *
 * @param amount payment amount, see [Amount]. Available payment options can vary depending on amount.
 * @param title name of the goods to be bought.
 * @param subtitle description of the goods to be bought.
 * @param clientApplicationKey merchant token from YooKassa.
 * @param shopId shop id from YooKassa.
 * @param paymentMethodId id of previous payment.
 * @param savePaymentMethod setting for saving payment method (see [SavePaymentMethod]]).
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class SavedBankCardPaymentParameters
@Keep constructor(
    @Keep val amount: Amount,
    @Keep val title: String,
    @Keep val subtitle: String,
    @Keep val clientApplicationKey: String,
    @Keep val shopId: String,
    @Keep val paymentMethodId: String,
    @Keep val savePaymentMethod: SavePaymentMethod
) : Parcelable
