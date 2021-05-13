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

package ru.yoomoney.sdk.kassa.payments.impl.logout

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.extensions.edit
import ru.yoomoney.sdk.kassa.payments.payment.SharedPreferencesCurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepository
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepositoryImpl
import ru.yoomoney.sdk.kassa.payments.model.AnonymousUser
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser

@RunWith(RobolectricTestRunner::class)
class LogoutGatewayImplTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var logoutRepository: LogoutRepository
    private lateinit var tokensStorage: TokensStorage
    private lateinit var currentUserRepository: SharedPreferencesCurrentUserRepository
    private val tmxSessionIdStorage = TmxSessionIdStorage()

    @Before
    fun setUp() {
        sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("testsp", Context.MODE_PRIVATE)
        tokensStorage = TokensStorage(
            preferences = sharedPreferences,
            encrypt = { it },
            decrypt = { it }
        )
        currentUserRepository =
            SharedPreferencesCurrentUserRepository(
                tokensStorage,
                sharedPreferences
            )
        logoutRepository = LogoutRepositoryImpl(
            currentUserRepository = currentUserRepository,
            userAuthInfoRepository = tokensStorage,
            paymentAuthTokenRepository = tokensStorage,
            removeKeys = { },
            tmxSessionIdStorage = tmxSessionIdStorage,
            revokeUserAuthToken = {},
            loadedPaymentOptionListRepository = mock()
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit {
            clear()
        }
    }

    @Test
     fun shouldRemoveUserData() = runBlocking<Unit> {
        // prepare
        currentUserRepository.currentUser = AuthorizedUser()
        tokensStorage.paymentAuthToken = "12345"
        tokensStorage.userAuthToken = "12345"

        // invoke
        logoutRepository.logout()

        // assert
        assertThat(currentUserRepository.currentUser, equalTo(AnonymousUser as CurrentUser))
        assertThat(tokensStorage.paymentAuthToken, nullValue())
        assertThat(tokensStorage.userAuthToken, nullValue())
    }
}
