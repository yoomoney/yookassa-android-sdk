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

package ru.yoo.sdk.kassa.payments.impl.paymentAuth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoo.sdk.kassa.payments.model.AuthType
import ru.yoo.sdk.kassa.payments.model.AuthType.EMERGENCY
import ru.yoo.sdk.kassa.payments.model.AuthType.NOT_NEEDED
import ru.yoo.sdk.kassa.payments.model.AuthType.OAUTH_TOKEN
import ru.yoo.sdk.kassa.payments.model.AuthType.PUSH
import ru.yoo.sdk.kassa.payments.model.AuthType.SMS
import ru.yoo.sdk.kassa.payments.model.AuthType.TOTP
import ru.yoo.sdk.kassa.payments.model.AuthType.UNKNOWN
import ru.yoo.sdk.kassa.payments.model.AuthTypeState

@RunWith(Parameterized::class)
internal class SelectAppropriateAuthTypeTest(
    private val defaultAuthType: AuthType,
    private val authTypes: Array<AuthType>,
    private val expected: Any
) {
    companion object {
        @[Parameterized.Parameters JvmStatic]
        fun data() = listOf(
                // default allowed, default present
                arrayOf(SMS, arrayOf(SMS, EMERGENCY), SMS),
                // default allowed, default not present
                arrayOf(SMS, arrayOf(TOTP, EMERGENCY), TOTP),
                // default not allowed
                arrayOf(PUSH, arrayOf(SMS, EMERGENCY), SMS),
                // noting found
                arrayOf(SMS, arrayOf<AuthType>(), NoSuchElementException::class.java),
                arrayOf(SMS, arrayOf(PUSH, OAUTH_TOKEN, NOT_NEEDED, UNKNOWN), NoSuchElementException::class.java)
        )
    }

    private val selectAppropriateAuthType = SelectAppropriateAuthType()

    @Test
    fun test() {
        // prepare
        val authTypeStates = Array(authTypes.size) {
            AuthTypeState(
                authTypes[it],
                null
            )
        }

        // invoke
        val result = try {
            selectAppropriateAuthType(defaultAuthType, authTypeStates).type
        } catch (e: Exception) {
            e
        }

        // assert
        when (result) {
            is Exception -> assertThat(result, instanceOf(expected as Class<*>))
            else -> assertThat(result, equalTo(expected))
        }
    }
}