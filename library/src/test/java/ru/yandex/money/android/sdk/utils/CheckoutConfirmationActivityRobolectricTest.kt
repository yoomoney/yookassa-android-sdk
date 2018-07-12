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

package ru.yandex.money.android.sdk.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.Shadows
import ru.yandex.money.android.sdk.Checkout
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class CheckoutConfirmationActivityRobolectricTest {

    @Test
    fun `should end with error if not https url`() {
        // prepare
        val url = URL("http://wrong.scheme.url/")
        val activityController = Robolectric.buildActivity(
            CheckoutConfirmationActivity::class.java,
            Checkout.create3dsIntent(application, url, url)
        )

        // invoke
        activityController.create()

        // assert
        val shadowActivity = Shadows.shadowOf(activityController.get())
        assertThat(shadowActivity.resultCode, equalTo(Checkout.RESULT_ERROR))

        with(shadowActivity.resultIntent) {
            assertThat(getIntExtra(Checkout.EXTRA_ERROR_CODE, 0), equalTo(Checkout.ERROR_NOT_HTTPS_URL))
            assertThat(getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION), equalTo("Not https:// url"))
            assertThat(getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL), equalTo(url.toString()))
        }
    }
}