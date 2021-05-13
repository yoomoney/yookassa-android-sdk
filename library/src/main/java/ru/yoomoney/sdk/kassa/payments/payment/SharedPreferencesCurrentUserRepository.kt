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

package ru.yoomoney.sdk.kassa.payments.payment

import android.content.SharedPreferences
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.extensions.edit
import ru.yoomoney.sdk.kassa.payments.model.AnonymousUser
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser

private const val KEY_CURRENT_USER_NAME = "current_user_name"

internal class SharedPreferencesCurrentUserRepository(
    private val tokensStorage: TokensStorage,
    private val sharedPreferences: SharedPreferences
) : CurrentUserRepository {

    override var currentUser: CurrentUser
        get() {
            return if (sharedPreferences.contains(KEY_CURRENT_USER_NAME)) {
                if (!tokensStorage.isAuthorizedByOldWay() && !tokensStorage.isAuthorized()) {
                    currentUser = AnonymousUser
                    AnonymousUser
                } else {
                    AuthorizedUser()
                }
            } else {
                AnonymousUser
            }
        }
        set(value) {
            sharedPreferences.edit {
                when (value) {
                    AnonymousUser -> remove(KEY_CURRENT_USER_NAME)
                    is AuthorizedUser -> putString(KEY_CURRENT_USER_NAME, "")
                }
            }
        }
}
