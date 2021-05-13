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

package ru.yoomoney.sdk.kassa.payments.impl

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config
import ru.yoomoney.sdk.kassa.payments.on
import ru.yoomoney.sdk.kassa.payments.http.UserAgentInterceptor

class UserAgentInterceptorTest {

    private val tetstUserAgent = "testUserAgent"
    private val chain = mock(Interceptor.Chain::class.java)
    private val request = Request.Builder()
        .url("http://test/")
        .build()

    init {
        on(chain.request()).thenReturn(request)
        on(chain.proceed(any() ?: request)).then {
            Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .code(0)
                .message("message")
                .request(it.arguments[0] as Request)
                .build()
        }
    }

    @Test
    fun `should be with smartphone when on smartphone`() {
        // prepare
        val userAgentInterceptor =
            UserAgentInterceptor(tetstUserAgent)

        // invoke
        val response = userAgentInterceptor.intercept(chain)

        // assert
        val userAgent = response.request.header("User-Agent")
        assertThat(userAgent, notNullValue())
        assertThat(
            userAgent,
            equalTo(userAgent)
        )
    }

    @[Test Config(qualifiers = "sw600dp")]
    fun `should be with tablet when smallest width more then 320 dp`() {
        // prepare
        val userAgentInterceptor =
            UserAgentInterceptor(tetstUserAgent)

        // invoke
        val response = userAgentInterceptor.intercept(chain)

        // assert
        val userAgent = response.request.header("User-Agent")
        assertThat(userAgent, notNullValue())
        assertThat(
            userAgent,
            equalTo(tetstUserAgent)
        )
    }
}
