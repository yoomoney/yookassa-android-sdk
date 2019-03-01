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

import android.annotation.SuppressLint
import android.os.Parcelable
import android.support.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Mock configuration. This parameters can be used only in offline test mode
 * @see [TestParameters] in [Checkout.createTokenizeIntent]
 *
 * @param completeWithError complete payment with error
 * @param paymentAuthPassed get preauthorized user
 * @param linkedCardsCount number of linked cards for authorized user
 */
@[Parcelize SuppressLint("ParcelCreator")]
data class MockConfiguration
@[JvmOverloads Keep] constructor(
    @get:Keep val completeWithError: Boolean = false,
    @get:Keep val paymentAuthPassed: Boolean = false,
    @get:Keep val linkedCardsCount: Int = 1
) : Parcelable
