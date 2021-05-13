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

package ru.yoomoney.sdk.kassa.payments.methods.paymentAuth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.hamcrest.core.IsNull.nullValue
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.extensions.toJsonObject
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueInitRequest
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
internal class CheckoutTokenIssueInitRequestTest {
    private val instanceName = "instanceName"
    private val singleAmountMax = Amount(BigDecimal.TEN, RUB)
    private val tmxSessionId = "tmxSessionId"
    private val userAuthToken = "userAuthToken"
    private val shopToken = "shopToken"

    @Test
    fun testPayload() {
        // prepare
        val request = CheckoutTokenIssueInitRequest(
            instanceName, singleAmountMax, false, tmxSessionId, userAuthToken, shopToken
        )

        // invoke
        val payload = request.getPayload()

        // assert
        assertThat(payload.size, equalTo(4))

        val iterator = payload.iterator()

        iterator.next().also { (key, value) ->
            assertThat(key, equalTo("instanceName"))
            assertThat(value, instanceOf(String::class.java))
            assertThat(value as String, equalTo(instanceName))
        }

        iterator.next().also { (key, value) ->
            assertThat(key, equalTo("paymentUsageLimit"))
            assertThat(value, instanceOf(String::class.java))
            assertThat(value as String, equalTo("Single"))
        }

        iterator.next().also { (key, value) ->
            assertThat(key, equalTo("tmxSessionId"))
            assertThat(value, instanceOf(String::class.java))
            assertThat(value as String, equalTo(tmxSessionId))
        }

        iterator.next().also { (key, value) ->
            assertThat(key, equalTo("singleAmountMax"))
            assertThat(value, instanceOf(JSONObject::class.java))
            assertThat(value.toString(), equalTo(singleAmountMax.toJsonObject().toString()))
        }
    }

    @Test
    fun testSingleUsage() {
        // prepare
        val request = CheckoutTokenIssueInitRequest(
            instanceName, singleAmountMax, false, tmxSessionId, userAuthToken, shopToken
        )

        // invoke
        val payload = request.getPayload()

        // assert
        payload.first { (key, _) -> key == "paymentUsageLimit" }.also { (_, value) ->
            assertThat(value, instanceOf(String::class.java))
            assertThat(value as String, equalTo("Single"))
        }

        payload.first { (key, _) -> key == "singleAmountMax" }.also { (_, value) ->
            assertThat(value, instanceOf(JSONObject::class.java))
            assertThat(value.toString(), equalTo(singleAmountMax.toJsonObject().toString()))
        }
    }

    @Test
    fun testMultipleUsage() {
        // prepare
        val request = CheckoutTokenIssueInitRequest(
            instanceName, singleAmountMax, true, tmxSessionId, userAuthToken, shopToken
        )

        // invoke
        val payload = request.getPayload()

        // assert
        payload.first { (key, _) -> key == "paymentUsageLimit" }.also { (_, value) ->
            assertThat(value, instanceOf(String::class.java))
            assertThat(value as String, equalTo("Multiple"))
        }

        val pair = payload.find { (key, _) -> key == "singleAmountMax" }
        assertThat(pair, nullValue())
    }
}