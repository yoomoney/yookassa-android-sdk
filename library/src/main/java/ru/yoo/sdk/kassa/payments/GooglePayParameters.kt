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

package ru.yoo.sdk.kassa.payments

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import ru.yoo.sdk.kassa.payments.GooglePayCardNetwork.AMEX
import ru.yoo.sdk.kassa.payments.GooglePayCardNetwork.DISCOVER
import ru.yoo.sdk.kassa.payments.GooglePayCardNetwork.JCB
import ru.yoo.sdk.kassa.payments.GooglePayCardNetwork.MASTERCARD
import ru.yoo.sdk.kassa.payments.GooglePayCardNetwork.VISA

/**
 * Settings for Google Pay payment method. This class is one of the parameters of [PaymentParameters].
 * @param allowedCardNetworks (optional) networks, that can be used by user. Google Pay will only show cards that belong
 * to this set. If no value is specified, the default set will be used.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class GooglePayParameters
@[JvmOverloads Keep] constructor(
    @Keep val allowedCardNetworks: Set<GooglePayCardNetwork> = setOf(AMEX, DISCOVER, JCB, VISA, MASTERCARD)
) : Parcelable

@Keep
enum class GooglePayCardNetwork {
    @Keep AMEX,
    @Keep DISCOVER,
    @Keep JCB,
    @Keep MASTERCARD,
    @Keep VISA,
    @Keep INTERAC,
    @Keep OTHER
}
