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

import android.content.Context
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.IsNull.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.on
import ru.yoomoney.sdk.kassa.payments.extensions.edit
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
internal class TokensStorageTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val sp = RuntimeEnvironment.application.getSharedPreferences("sp", Context.MODE_PRIVATE).apply {
        edit { clear() }
    }

    @Mock
    private lateinit var decrypt: Cryptor
    @Mock
    private lateinit var encrypt: Cryptor

    private lateinit var tokensStorage: TokensStorage

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        on(encrypt(any() ?: "")).then { it.arguments[0] }
        on(decrypt(any() ?: "")).then { it.arguments[0] }

        tokensStorage =
            TokensStorage(sp, encrypt::invoke, decrypt::invoke)
    }

    @Test
    fun `write null to userAuthToken should remove userAuthToken`() {
        // prepare
        tokensStorage.userAuthToken = "token"

        // invoke
        tokensStorage.userAuthToken = null

        // assert
        assertThat(tokensStorage.userAuthToken, nullValue())
    }

    @Test
    fun `write null to paymentAuthToken should remove paymentAuthToken`() {
        // prepare
        tokensStorage.paymentAuthToken = "token"

        // invoke
        tokensStorage.paymentAuthToken = null

        // assert
        assertThat(tokensStorage.paymentAuthToken, nullValue())
    }

    @Test
    fun `write null to paymentAuthToken after persisting should remove paymentAuthToken`() {
        // prepare
        tokensStorage.paymentAuthToken = "token"
        tokensStorage.persistPaymentAuth()

        // invoke
        tokensStorage.paymentAuthToken = null

        // assert
        assertThat(tokensStorage.paymentAuthToken, nullValue())
    }

    @Test
    fun `get userAuthToken without set should return null`() {
        // prepare

        // invoke
        val userAuth = tokensStorage.userAuthToken

        // assert
        assertThat(userAuth, nullValue())
    }

    @Test
    fun `get paymentAuthToken without set should return null`() {
        // prepare

        // invoke
        val paymentAuth = tokensStorage.paymentAuthToken

        // assert
        assertThat(paymentAuth, nullValue())
    }

    @Test
    fun `paymentAuthToken and userAuthToken should have different values`() {
        // prepare

        // invoke
        tokensStorage.paymentAuthToken = "paymentAuth"
        tokensStorage.userAuthToken = "userAuth"

        // assert
        assertThat(tokensStorage.paymentAuthToken, not(tokensStorage.userAuthToken))
    }

    @Test
    fun `should invoke encrypt when write userAuthToken`() {
        // prepare
        val token = "user auth"

        // invoke
        tokensStorage.userAuthToken = token

        // assert
        verify(encrypt).invoke(token)
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should invoke encrypt when write and persist paymentAuthToken`() {
        // prepare
        val token = "payment auth"

        // invoke
        tokensStorage.paymentAuthToken = token
        tokensStorage.persistPaymentAuth()

        // assert
        verify(encrypt).invoke(token)
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should not invoke encrypt when write and not persist paymentAuthToken`() {
        // prepare
        val token = "payment auth"

        // invoke
        tokensStorage.paymentAuthToken = token

        // assert
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should invoke decrypt when read userAuthToken`() {
        // prepare
        val token = "user auth"
        tokensStorage.userAuthToken = token
        clearInvocations(encrypt)

        // invoke
        val resultToken = tokensStorage.userAuthToken

        // assert
        assertThat(resultToken, equalTo(token))
        verify(decrypt).invoke(token)
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should invoke decrypt when read persisted paymentAuthToken`() {
        // prepare
        val token = "payment auth"
        tokensStorage.paymentAuthToken = token
        tokensStorage.persistPaymentAuth()
        clearInvocations(encrypt)
        tokensStorage =
            TokensStorage(sp, encrypt::invoke, decrypt::invoke)

        // invoke
        val resultToken = tokensStorage.paymentAuthToken

        // assert
        assertThat(resultToken, equalTo(token))
        verify(decrypt).invoke(token)
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should not invoke decrypt when read not persisted paymentAuthToken`() {
        // prepare
        val token = "payment auth"
        tokensStorage.paymentAuthToken = token
        clearInvocations(encrypt)

        // invoke
        val resultToken = tokensStorage.paymentAuthToken

        // assert
        assertThat(resultToken, equalTo(token))
        verifyNoMoreInteractions(encrypt, decrypt)
    }

    @Test
    fun `should return true when checkPaymentAuthRequired if no paymentAuthToken set`() {
        // prepare

        // invoke
        val authRequired = tokensStorage.checkPaymentAuthRequired()

        // assert
        assertThat("required", authRequired)
    }

    @Test
    fun `should return false when checkPaymentAuthRequired if paymentAuthToken set and not persisted`() {
        // prepare
        tokensStorage.paymentAuthToken = "test token"

        // invoke
        val authRequired = tokensStorage.checkPaymentAuthRequired()

        // assert
        assertThat("not required", !authRequired)
    }

    @Test
    fun `should return false when checkPaymentAuthRequired if paymentAuthToken set and persisted`() {
        // prepare
        with(tokensStorage) {
            paymentAuthToken = "test token"
            persistPaymentAuth()
        }

        // invoke
        val authRequired = tokensStorage.checkPaymentAuthRequired()

        // assert
        assertThat("not required", !authRequired)
    }

    private interface Cryptor {
        operator fun invoke(s: String): String
    }
}