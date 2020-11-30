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

package ru.yoo.sdk.kassa.payments.utils

import android.app.Activity.RESULT_OK
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.webkit.WebViewClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.yoo.sdk.kassa.payments.Checkout
import ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_CODE
import ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_DESCRIPTION
import ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_FAILING_URL
import ru.yoo.sdk.kassa.payments.Checkout.RESULT_ERROR
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class WebViewActivityTest {

    @[Rule JvmField]
    val checkoutConfirmationActivityRule = ActivityTestRule(WebViewActivity::class.java, false, false)

    @Test
    fun shouldEndsWith_ResultOk_When_RedirectUrlReached() {
        // preapre
        val context = InstrumentationRegistry.getInstrumentation().context
        val intent = Checkout.create3dsIntent(
            context = context,
            url = "file:///android_asset/test.html"
        )
        checkoutConfirmationActivityRule.launchActivity(intent)

        // invoke
        onWebView()
            .withElement(findElement(Locator.LINK_TEXT, "redirect"))
            .perform(webClick())

        // assert
        assertThat(checkoutConfirmationActivityRule.activityResult.resultCode, equalTo(RESULT_OK))
    }

    @Test
    fun shouldEndsWith_ResultError_When_RequestExecutionFailed() {
        // preapre
        val context = InstrumentationRegistry.getInstrumentation().context
        val url = "file:///android_asset/test.html"
        val intent = Checkout.create3dsIntent(
            context = context,
            url = url
        )
        checkoutConfirmationActivityRule.launchActivity(intent)

        // invoke
        onWebView()
            .withElement(findElement(Locator.LINK_TEXT, "fail"))
            .perform(webClick())
        sleep(1000)

        // assert
        assertThat(checkoutConfirmationActivityRule.activityResult.resultCode, equalTo(RESULT_ERROR))
        val resultData = checkoutConfirmationActivityRule.activityResult.resultData
        assertThat(resultData.getStringExtra(EXTRA_URL), equalTo(url))
        assertThat(resultData.getIntExtra(EXTRA_ERROR_CODE, 0), equalTo(WebViewClient.ERROR_HOST_LOOKUP))
        assertThat(resultData.getStringExtra(EXTRA_ERROR_DESCRIPTION), notNullValue())
        assertThat(resultData.getStringExtra(EXTRA_ERROR_FAILING_URL), equalTo("https://123.456.789.qwq/"))
    }
}
