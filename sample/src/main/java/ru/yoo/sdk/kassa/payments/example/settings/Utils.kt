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

package ru.yoo.sdk.kassa.payments.example.settings

import android.content.Context
import ru.yoo.sdk.kassa.payments.example.App
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_AUTOFILL_USER_PHONE_NUMBER
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_GOOGLE_PAY_ALLOWED
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_LINKED_CARDS_COUNT
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_NEW_CARD_ALLOWED
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_PAYMENT_AUTH_PASSED
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SAVE_PAYMENT_METHOD
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SBERBANK_ONLINE_ALLOWED
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SERVICE_FEE
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SHOW_CHECKOUT_LOGO
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_TEST_MODE_ENABLED
import ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_YOO_MONEY_ALLOWED

internal fun Context.injectParameters(
    isYooMoneyAllowed: Boolean = true,
    isSberAllowed: Boolean = true,
    isGooglePayAllowed: Boolean = true,
    isNewCardAllowed: Boolean = true,
    isTestModeEnabled: Boolean = false,
    serviceFee: Float = 0.00f,
    savePaymentMethodId: Int = 0,
    shouldShowCheckoutLogo: Boolean = true,
    shouldAutoFillUserPhoneNumber: Boolean = true,
    shouldPassPaymentAuth: Boolean = true,
    shouldCompletePaymentWithError: Boolean = false,
    linkedCardsCount: Int = 1
) {
    val editor = Settings(applicationContext).sp.edit()

    mapOf(
        KEY_YOO_MONEY_ALLOWED to isYooMoneyAllowed,
        KEY_SBERBANK_ONLINE_ALLOWED to isSberAllowed,
        KEY_GOOGLE_PAY_ALLOWED to isGooglePayAllowed,
        KEY_NEW_CARD_ALLOWED to isNewCardAllowed,
        KEY_TEST_MODE_ENABLED to isTestModeEnabled,
        KEY_SHOW_CHECKOUT_LOGO to shouldShowCheckoutLogo,
        KEY_AUTOFILL_USER_PHONE_NUMBER to shouldAutoFillUserPhoneNumber,
        KEY_PAYMENT_AUTH_PASSED to shouldPassPaymentAuth,
        KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR to shouldCompletePaymentWithError
    ).forEach {
        editor.putBoolean(it.key, it.value)
    }

    editor.putFloat(KEY_SERVICE_FEE, serviceFee)
    editor.putInt(KEY_SAVE_PAYMENT_METHOD, savePaymentMethodId)
    editor.putInt(KEY_LINKED_CARDS_COUNT, linkedCardsCount)

    editor.apply()
}