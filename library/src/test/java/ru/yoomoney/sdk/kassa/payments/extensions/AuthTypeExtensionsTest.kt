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

package ru.yoomoney.sdk.kassa.payments.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.extensions.toHint
import ru.yoomoney.sdk.kassa.payments.model.AuthType

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class AuthTypeExtensionsTest(
    private val authType: AuthType,
    private val stringResId: Int?
) {

    companion object {
        @[ParameterizedRobolectricTestRunner.Parameters(name = "AuthType.{0}") JvmStatic]
        fun data() = listOf(
                arrayOf(AuthType.SMS, R.string.ym_auth_hint_sms),
                arrayOf(AuthType.TOTP, R.string.ym_auth_hint_totp),
                arrayOf(AuthType.SECURE_PASSWORD, R.string.ym_auth_hint_password),
                arrayOf(AuthType.EMERGENCY, R.string.ym_auth_hint_emergency),
                arrayOf(AuthType.PUSH, null),
                arrayOf(AuthType.OAUTH_TOKEN, null),
                arrayOf(AuthType.UNKNOWN, null)
        )
    }

    private val context = RuntimeEnvironment.application

    @Test
    fun testToHint() {
        try {
            assertThat(authType.toHint(context), equalTo(stringResId?.let { context.getText(it) }))
        } catch (e: IllegalArgumentException) {
            assertThat(data().first { it[0] == authType }[1], nullValue())
        }
    }
}