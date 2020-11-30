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

package ru.yoo.sdk.kassa.payments.impl.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mock
import ru.yoo.sdk.kassa.payments.model.AnonymousUser
import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.CurrentUser
import ru.yoo.sdk.kassa.payments.on
import ru.yoo.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoo.sdk.kassa.payments.payment.CurrentUserGateway

@RunWith(Parameterized::class)
internal class UserAuthTypeParamProviderTest(
    private val currentUser: CurrentUser,
    private val authRequired: Boolean,
    private val expected: AuthType
) {

    companion object {
        @[Parameterized.Parameters(name = "{0},{1},{2}") JvmStatic]
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(AnonymousUser, true, AuthTypeWithoutAuth()),
                    arrayOf(AnonymousUser, false, AuthTypeWithoutAuth()),
                    arrayOf(AuthorizedUser(), true, AuthTypeYooMoneyLogin()),
                    arrayOf(AuthorizedUser(), false, AuthTypePaymentAuth())
            )
        }
    }

    private val currentUserGateway = mock(CurrentUserGateway::class.java)
    private val paymentAuthRequiredGateway = mock(CheckPaymentAuthRequiredGateway::class.java)
    private val getUserAuthTypeParam = UserAuthTypeParamProvider(currentUserGateway, paymentAuthRequiredGateway)

    @Test
    fun test() {
        // prepare
        on(currentUserGateway.currentUser).thenReturn(currentUser)
        on(paymentAuthRequiredGateway.checkPaymentAuthRequired()).thenReturn(authRequired)

        // invoke
        val authTypeParam = getUserAuthTypeParam()

        // assert
        assertThat(authTypeParam, equalTo(expected))
    }
}
