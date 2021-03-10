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
import ru.yoo.sdk.kassa.payments.utils.getAllPaymentMethods

/**
 * Wrapper for payment parameters. This class is used in [Checkout.createTokenizeIntent].
 *
 * @param amount payment amount, see [Amount]. Available payment options can vary depending on amount.
 * @param title name of the goods to be bought.
 * @param subtitle description of the goods to be bought.
 * @param clientApplicationKey merchant token from YooKassa.
 * @param shopId shop id from YooKassa.
 * @param savePaymentMethod setting for saving payment method (see [SavePaymentMethod]]).
 * @param paymentMethodTypes (optional) set of permitted method types. Empty set or parameter absence means that all
 * payment methods are allowed. See [PaymentMethodType].
 * @param gatewayId (optional) your gateway id from YooMoney.
 * @param customReturnUrl (optional) redirect url for custom 3ds. This parameter should be used only if you want to use
 * your own 3ds activity. If you are using [Checkout.create3dsIntent], do not set this parameter.
 * @param userPhoneNumber (optional) phone number of the user. It will be inserted into the form
 * that is used by "Sberbank online" payment method. Format for this number is "+7XXXXXXXXXX".
 * @param googlePayParameters (optional) settings for Google Pay (see [GooglePayParameters]).
 * * @param authCenterClientId (optional) client id for ru.yoo.sdk.auth:auth sdk.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class PaymentParameters
@[JvmOverloads Keep] constructor(
    @Keep val amount: Amount,
    @Keep val title: String,
    @Keep val subtitle: String,
    @Keep val clientApplicationKey: String,
    @Keep val shopId: String,
    @Keep val savePaymentMethod: SavePaymentMethod,
    @Keep val paymentMethodTypes: Set<PaymentMethodType> = getAllPaymentMethods(),
    @Keep val gatewayId: String? = null,
    @Keep val customReturnUrl: String? = null,
    @Keep val userPhoneNumber: String? = null,
    @Keep val googlePayParameters: GooglePayParameters = GooglePayParameters(),
    @Keep val authCenterClientId: String? = null
) : Parcelable
