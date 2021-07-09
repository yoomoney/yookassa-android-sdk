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

package ru.yoomoney.sdk.kassa.payments.secure

import android.content.SharedPreferences
import ru.yoomoney.sdk.kassa.payments.extensions.set
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository

private const val KEY_USER_AUTH_TOKEN = "userAuthToken"
private const val KEY_YOO_USER_AUTH_TOKEN = "yooUserAuthToken"
private const val KEY_YOO_USER_AUTH_NAME = "yooUserAuthName"
private const val KEY_YOO_USER_AVATAR_URL = "yooUserAvatarUrl"
private const val KEY_YOO_USER_IS_REMEMBER = "isYooUserRemember"
private const val KEY_PAYMENT_AUTH_TOKEN = "paymentAuthToken"

internal class TokensStorage(
    private val preferences: SharedPreferences,
    private val encrypt: (String) -> String,
    private val decrypt: (String) -> String
) : CheckPaymentAuthRequiredGateway, UserAuthInfoRepository, PaymentAuthTokenRepository {

    private var _paymentAuthToken: String? = null

    fun isAuthorizedByOldWay(): Boolean {
        return !passportAuthToken.isNullOrEmpty() && !paymentAuthToken.isNullOrEmpty()
    }

    fun isAuthorized(): Boolean {
        return !userAuthToken.isNullOrEmpty()
    }

    override var userAuthToken: String?
        get() {
            return preferences.getString(KEY_YOO_USER_AUTH_TOKEN, null)?.let(decrypt)
        }
        set(value) {
            preferences[KEY_YOO_USER_AUTH_TOKEN] = value?.let(encrypt)
        }

    override var userAuthName: String?
        get() = preferences.getString(KEY_YOO_USER_AUTH_NAME, null)
        set(value) {
            preferences[KEY_YOO_USER_AUTH_NAME] = value
        }

    override var userAvatarUrl: String?
        get() = preferences.getString(KEY_YOO_USER_AVATAR_URL, null)
        set(value) {
            preferences[KEY_YOO_USER_AVATAR_URL] = value
        }

    override var passportAuthToken: String?
        get() = preferences.getString(KEY_USER_AUTH_TOKEN, null)?.let(decrypt)
        set(value) {
            preferences[KEY_USER_AUTH_TOKEN] = value?.let(encrypt)
        }

    override var paymentAuthToken: String?
        get() = _paymentAuthToken ?: preferences.getString(KEY_PAYMENT_AUTH_TOKEN, null)?.let(decrypt)
        set(value) {
            _paymentAuthToken = value
            if (value == null) {
                preferences[KEY_PAYMENT_AUTH_TOKEN] = null
            }
        }

    override var isUserAccountRemember: Boolean
        get() = preferences.getBoolean(KEY_YOO_USER_IS_REMEMBER, true)
        set(value) {
            preferences[KEY_YOO_USER_IS_REMEMBER] = value
        }

    override val isPaymentAuthPersisted: Boolean
        get() = KEY_PAYMENT_AUTH_TOKEN in preferences

    override fun persistPaymentAuth() {
        preferences[KEY_PAYMENT_AUTH_TOKEN] = paymentAuthToken?.let(encrypt)
    }

    override fun checkUserAccountRemember(): Boolean = isUserAccountRemember

    override fun checkPaymentAuthRequired() =
        _paymentAuthToken == null && preferences.getString(KEY_PAYMENT_AUTH_TOKEN, null) == null

    fun reset() {
        _paymentAuthToken = null
        preferences[KEY_PAYMENT_AUTH_TOKEN] = null
        preferences[KEY_YOO_USER_IS_REMEMBER] = null
    }
}
