/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk

import android.support.annotation.Keep

/**
 * Wrapper for parameters, that doesn't change for different payments.
 * @param title shop title
 * @param subtitle shop description
 * @param clientApplicationKey merchant token from Yandex.Checkout
 * @param paymentMethodTypes set of permitted method types
 * @param enableGooglePay enable Google Pay
 * @param shopId shop id from Yandex.Checkout
 * @param gatewayId (optional) your gateway id from Yandex.Money
 * @param showLogo show or hide Yandex.Checkout logo on payment options screen
 */
data class ShopParameters @[JvmOverloads Keep] constructor(
    @get:Keep val title: String,
    @get:Keep val subtitle: String,
    @get:Keep val clientApplicationKey: String,
    @get:Keep val paymentMethodTypes: Set<PaymentMethodType> = emptySet(),
    @get:Keep val enableGooglePay: Boolean = false,
    @get:Keep val shopId: String? = null,
    @get:Keep val gatewayId: String? = null,
    @get:Keep val showLogo: Boolean = true
)
