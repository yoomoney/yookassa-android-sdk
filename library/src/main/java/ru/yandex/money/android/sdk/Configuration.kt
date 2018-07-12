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
 * Test mode configuration
 * @param enableTestMode use mock data source instead of backend connection
 * @param completeWithError complete payment with error
 * @param paymentAuthPassed user preauthorized
 * @param linkedCardsCount count of linked cards for authorized user
 * @param googlePayAvailable emulate google pay availability
 * @param googlePayTestEnvironment use google pay test environment
 */
data class Configuration @Keep constructor(
    @get:Keep val enableTestMode: Boolean,
    @get:Keep val completeWithError: Boolean,
    @get:Keep val paymentAuthPassed: Boolean,
    @get:Keep val linkedCardsCount: Int,
    @get:Keep val googlePayAvailable: Boolean = false,
    @get:Keep val googlePayTestEnvironment: Boolean = true
)
