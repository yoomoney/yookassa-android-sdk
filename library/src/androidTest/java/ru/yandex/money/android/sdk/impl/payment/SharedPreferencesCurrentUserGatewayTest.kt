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

package ru.yandex.money.android.sdk.impl.payment

import android.content.Context
import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.yandex.money.android.sdk.AnonymousUser
import ru.yandex.money.android.sdk.AuthorizedUser
import ru.yandex.money.android.sdk.CurrentUser

@RunWith(AndroidJUnit4::class)
class SharedPreferencesCurrentUserGatewayTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gateway: SharedPreferencesCurrentUserGateway

    @Before
    fun setUp() {
        sharedPreferences = InstrumentationRegistry.getContext().getSharedPreferences("testsp", Context.MODE_PRIVATE)
        gateway = SharedPreferencesCurrentUserGateway(sharedPreferences)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun shouldReturn_AnonymousUser_When_NotModified() {
        // prepare

        // invoke
        val currentUser: CurrentUser = gateway.currentUser

        // assert
        assertThat(currentUser, equalTo(AnonymousUser as CurrentUser))
    }

    @Test
    fun shouldReturn_AuthorizedUser_When_AuthorizedUserSet() {
        // prepare
        val authorizedUser = AuthorizedUser("test name")
        gateway.currentUser = authorizedUser

        // invoke
        val currentUser = gateway.currentUser

        // assert
        assertThat(currentUser, equalTo(authorizedUser as CurrentUser))
    }

    @Test
    fun shouldReturn_AnonymousUser_When_AuthorizedUserChangedToAnonymousUser() {
        // prepare
        val authorizedUser = AuthorizedUser("test name")
        gateway.currentUser = authorizedUser
        gateway.currentUser = AnonymousUser

        // invoke
        val currentUser = gateway.currentUser

        // assert
        assertThat(currentUser, equalTo(AnonymousUser as CurrentUser))
    }
}
