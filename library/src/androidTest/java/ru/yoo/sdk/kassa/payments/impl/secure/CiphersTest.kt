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

package ru.yoo.sdk.kassa.payments.impl.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.crypto.spec.SecretKeySpec

@RunWith(AndroidJUnit4::class)
class CiphersTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val key = SecretKeySpec(ByteArray(16, Int::toByte), "AES")
    private val iv = ByteArray(16, Int::toByte)
    private val encrypt: (String) -> String = Encrypter({ key }, { iv })
    private val decrypt: (String) -> String = Decrypter({ key }, { iv })

    @Test
    fun encryptedShouldNotBeTheSameAsInput() {
        // prepare
        val input = "test string"

        // invoke
        val encrypted = encrypt(input)

        // assert
        MatcherAssert.assertThat(encrypted, IsNot.not(input))
    }

    @Test
    fun decryptedShouldNotBeTheSameAsInput() {
        // prepare
        val input = encrypt("test string")

        // invoke
        val decrypted = decrypt(input)

        // assert
        MatcherAssert.assertThat(decrypted, IsNot.not(input))
    }

    @Test
    fun encryptDecrypt() {
        // prepare
        val input = "test string"

        // invoke
        val decrypted = decrypt(encrypt(input))

        // assert
        MatcherAssert.assertThat(input, IsEqual.equalTo(decrypted))
    }
}
