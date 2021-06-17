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

package ru.yoomoney.sdk.kassa.payments.impl.userAuth

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.metrics.ActionMoneyAuthLoginCanceled
import ru.yoomoney.sdk.kassa.payments.metrics.ActionMoneyAuthLoginFail
import ru.yoomoney.sdk.kassa.payments.metrics.ActionMoneyAuthLoginSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginSchemeAuthSdk
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginSchemeYooMoney
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuth
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthAnalytics

class MoneyAuthAnalyticsTest {
    private val reporter: Reporter = mock()
    private val moneyAuthAnalytics = MoneyAuthAnalytics(
        reporter = reporter,
        businessLogic = mock()
    )

    @Test
    fun `verify money auth from auth sdk success analytics sends`() {
        // given

        // when
        moneyAuthAnalytics(
            MoneyAuth.State.CompleteAuth,
            MoneyAuth.Action.Authorized("token", null, null, MoneyAuthLoginSchemeAuthSdk())
        )

        // then
        verify(reporter).report(
            "actionMoneyAuthLogin",
            listOf(ActionMoneyAuthLoginSuccess(), MoneyAuthLoginSchemeAuthSdk())
        )
    }

    @Test
    fun `verify money auth from app success analytics sends`() {
        // given

        // when
        moneyAuthAnalytics(
            MoneyAuth.State.CompleteAuth,
            MoneyAuth.Action.Authorized("token", null, null, MoneyAuthLoginSchemeYooMoney())
        )

        // then
        verify(reporter).report(
            "actionMoneyAuthLogin",
            listOf(ActionMoneyAuthLoginSuccess(), MoneyAuthLoginSchemeYooMoney())
        )
    }

    @Test
    fun `verify money auth from auth sdk cancelled analytics sends`() {
        // given

        // when
        moneyAuthAnalytics.invoke(
            MoneyAuth.State.CompleteAuth,
            MoneyAuth.Action.AuthCancelled
        )

        // then
        verify(reporter).report(
            "actionMoneyAuthLogin",
            listOf(ActionMoneyAuthLoginCanceled(), MoneyAuthLoginSchemeAuthSdk())
        )
    }

    @Test
    fun `verify money auth from app failed analytics sends`() {
        // given

        // when
        moneyAuthAnalytics.invoke(
            MoneyAuth.State.CompleteAuth,
            MoneyAuth.Action.AuthFailed
        )

        // then
        verify(reporter).report(
            "actionMoneyAuthLogin",
            listOf(ActionMoneyAuthLoginFail(), MoneyAuthLoginSchemeYooMoney())
        )
    }
}