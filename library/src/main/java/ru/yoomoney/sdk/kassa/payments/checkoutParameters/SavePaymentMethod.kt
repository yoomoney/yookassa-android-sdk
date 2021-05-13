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

package ru.yoomoney.sdk.kassa.payments.checkoutParameters

import androidx.annotation.Keep

/**
 * Setting for saving payment method. If payment method is saved, shop can make recurring payments with a token,
 * received from [Checkout.createTokenizationResult].
 *
 * There are three options for this setting:
 * [ON] - always save payment method. User can select only from payment methods, that allow saving.
 * On the contract screen user will see a message about saving his payment method.
 * [OFF] - don't save payment method. User can select from all of the available payment methods.
 * [USER_SELECTS] - user chooses to save payment method (user will have a switch to change
 * selection, if payment method can be saved).
 */
@Keep
enum class SavePaymentMethod {
    /**
     * Always save payment method. User can select only from payment methods, that allow saving.
     * On the contract screen user will see a message about saving his payment method.
     */
    @Keep
    ON,
    /**
     * Don't save payment method. User can select from all of the available payment methods.
     */
    @Keep
    OFF,
    /**
     * User chooses to save payment method (user will have a switch to change selection,
     * if payment method can be saved).
     */
    @Keep
    USER_SELECTS
}
