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

package ru.yoomoney.sdk.kassa.payments.impl.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yoomoney.sdk.kassa.payments.extensions.toJsonObject
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.model.RedirectConfirmation

@RunWith(RobolectricTestRunner::class)
class JsonSerializationTest {
    @Test
    fun `test Confirmation toJsonObject`() {
        // prepare
        val returnUrl = "test redirect url"
        val confirmations = listOf(
            NoConfirmation,
            ExternalConfirmation,
            RedirectConfirmation(returnUrl),
            MobileApplication(returnUrl)
        )
        val expectedJsons = arrayOf(
            null.toString(),
            """{"type":"external"}""",
            """{"type":"redirect","return_url":"$returnUrl"}""",
            """{"type":"mobile_application","return_url":"$returnUrl","app_os_type":"android"}"""
        )

        // invoke
        val actuals = confirmations.asSequence()
            .map(Confirmation::toJsonObject)
            .map(JSONObject?::toString)
            .toList()

        // assert
        assertThat(actuals, contains(*expectedJsons))
    }
}
