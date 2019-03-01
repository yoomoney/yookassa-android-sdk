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

package ru.yandex.money.android.sdk.impl.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yandex.money.android.sdk.model.Confirmation
import ru.yandex.money.android.sdk.model.ExternalConfirmation
import ru.yandex.money.android.sdk.model.NoConfirmation
import ru.yandex.money.android.sdk.model.RedirectConfirmation

@RunWith(RobolectricTestRunner::class)
class JsonSerializationTest {
    @Test
    fun `test Confirmation toJsonObject`() {
        // prepare
        val returnUrl = "test redirect url"
        val confirmations = listOf(
            NoConfirmation,
            ExternalConfirmation,
            RedirectConfirmation(returnUrl)
        )
        val expectedJsons = arrayOf(
            null.toString(),
            """{"type":"external"}""",
            """{"type":"redirect","return_url":"$returnUrl"}"""
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
