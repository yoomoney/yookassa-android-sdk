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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.logoUrl
import ru.yoomoney.sdk.kassa.payments.metrics.AuthType
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTypePaymentAuth
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTypeWithoutAuth
import ru.yoomoney.sdk.kassa.payments.metrics.AuthTypeYooMoneyLogin
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeScheme
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeBankCard
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeLinkedCard
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeRecurring
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeSbolSms
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeWallet
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListAnalytics
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListNoWalletOutputModel
import java.lang.RuntimeException

@RunWith(Parameterized::class)
internal class PaymentOptionListAnalyticsTest(
    val authType: AuthType,
    val tokenizeScheme: TokenizeScheme?
) {

    private val analyticsSender: Reporter = mock()
    private val analytics: PaymentOptionListAnalytics =
        PaymentOptionListAnalytics(
            reporter = analyticsSender,
            businessLogic = mock(),
            getUserAuthType = { authType },
            getTokenizeScheme = { tokenizeScheme }
        )

    companion object {
        @[Parameterized.Parameters JvmStatic]
        fun data() = listOf(
            arrayOf(AuthTypeWithoutAuth(), null),
            arrayOf(AuthTypeYooMoneyLogin(), null),
            arrayOf(AuthTypePaymentAuth(), null),

            arrayOf(
                AuthTypeWithoutAuth(),
                TokenizeSchemeWallet()
            ),
            arrayOf(
                AuthTypeWithoutAuth(),
                TokenizeSchemeLinkedCard()
            ),
            arrayOf(
                AuthTypeWithoutAuth(),
                TokenizeSchemeBankCard()
            ),
            arrayOf(
                AuthTypeWithoutAuth(),
                TokenizeSchemeSbolSms()
            ),
            arrayOf(
                AuthTypeWithoutAuth(),
                TokenizeSchemeRecurring()
            ),

            arrayOf(
                AuthTypeYooMoneyLogin(),
                TokenizeSchemeWallet()
            ),
            arrayOf(
                AuthTypeYooMoneyLogin(),
                TokenizeSchemeLinkedCard()
            ),
            arrayOf(
                AuthTypeYooMoneyLogin(),
                TokenizeSchemeBankCard()
            ),
            arrayOf(
                AuthTypeYooMoneyLogin(),
                TokenizeSchemeSbolSms()
            ),
            arrayOf(
                AuthTypeYooMoneyLogin(),
                TokenizeSchemeRecurring()
            ),

            arrayOf(
                AuthTypePaymentAuth(),
                TokenizeSchemeWallet()
            ),
            arrayOf(
                AuthTypePaymentAuth(),
                TokenizeSchemeLinkedCard()
            ),
            arrayOf(
                AuthTypePaymentAuth(),
                TokenizeSchemeBankCard()
            ),
            arrayOf(
                AuthTypePaymentAuth(),
                TokenizeSchemeSbolSms()
            ),
            arrayOf(
                AuthTypePaymentAuth(),
                TokenizeSchemeRecurring()
            )
        )
    }

    @Test
    fun `should send screenPaymentOptions analytics`() {
        // given

        // when
        analytics.invoke(
            PaymentOptionList.State.Loading(logoUrl),
            PaymentOptionList.Action.LoadPaymentOptionListSuccess(PaymentOptionListNoWalletOutputModel(emptyList()))
        )

        // then
        verify(analyticsSender).report("screenPaymentOptions", listOf(authType))

    }

    @Test
    fun `should send screenError analytics`() {
        // given
        val expectedParams = tokenizeScheme?.let { listOf(authType, it) } ?: listOf(authType)

        // when
        analytics.invoke(
            PaymentOptionList.State.Loading(logoUrl),
            PaymentOptionList.Action.LoadPaymentOptionListFailed(RuntimeException())
        )

        // then
        verify(analyticsSender).report("screenError", expectedParams)

    }
}