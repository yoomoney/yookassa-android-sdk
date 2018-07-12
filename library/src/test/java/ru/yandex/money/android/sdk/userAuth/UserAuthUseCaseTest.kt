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

package ru.yandex.money.android.sdk.userAuth

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.CurrentUserGateway

@RunWith(MockitoJUnitRunner.StrictStubs::class)
internal class UserAuthUseCaseTest {

    @Mock
    private lateinit var authorizeUserGateway: AuthorizeUserGateway
    @Mock
    private lateinit var currentUserGateway: CurrentUserGateway
    @Mock
    private lateinit var userAuthTokenGateway: UserAuthTokenGateway
    @Mock
    private lateinit var walletCheckGateway: WalletCheckGateway

    private lateinit var useCase: UserAuthUseCase

    @Before
    fun setUp() {
        useCase = UserAuthUseCase(
                authorizeUserGateway = authorizeUserGateway,
                currentUserGateway = currentUserGateway,
                userAuthTokenGateway = userAuthTokenGateway,
                walletCheckGateway = walletCheckGateway
        )
    }

    @Test
    fun shouldReturn_AuthorizedUser_If_GatewayReturnsUser() {
        // prepare
        val testUserAuth = AuthorizeUserGateway.User("name", "user auth token")
        on(authorizeUserGateway.authorizeUser()).thenReturn(testUserAuth)
        on(walletCheckGateway.checkIfUserHasWallet("user auth token")).thenReturn(true)

        // invoke
        val model = useCase(Unit) as UserAuthSuccessOutputModel

        // assert
        assertThat(model.authorizedUser.userName, equalTo(testUserAuth.name))
        verify(authorizeUserGateway).authorizeUser()
        verify(userAuthTokenGateway).userAuthToken = testUserAuth.token
        verify(currentUserGateway).currentUser = model.authorizedUser
        verifyNoMoreInteractions(authorizeUserGateway, userAuthTokenGateway, currentUserGateway, walletCheckGateway)
    }

    @Test
    fun shouldReturn_UserAuthCanceled_If_GatewayReturnsNull() {
        // prepare

        // invoke
        useCase(Unit) as UserAuthCancelledOutputModel

        // assert
        verify(authorizeUserGateway).authorizeUser()
        verifyNoMoreInteractions(authorizeUserGateway, userAuthTokenGateway, currentUserGateway, walletCheckGateway)
    }

    @Test
    fun shouldReturn_UserNoWallet_If_GatewayReturnsNoWallet() {
        // prepare
        val testUserAuth = AuthorizeUserGateway.User("name", "user auth token")
        on(authorizeUserGateway.authorizeUser()).thenReturn(testUserAuth)
        on(walletCheckGateway.checkIfUserHasWallet("user auth token")).thenReturn(false)

        // invoke
        useCase(Unit) as UserAuthNoWalletOutputModel

        // assert
        verify(authorizeUserGateway).authorizeUser()
        verifyNoMoreInteractions(authorizeUserGateway, userAuthTokenGateway, currentUserGateway, walletCheckGateway)
    }
}
