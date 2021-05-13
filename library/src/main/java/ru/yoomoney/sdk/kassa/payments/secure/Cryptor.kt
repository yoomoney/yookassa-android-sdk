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

package ru.yoomoney.sdk.kassa.payments.secure

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

internal sealed class Cryptor(
        private val opMode: Int,
        private val getKey: () -> Key,
        private val getIv: () -> ByteArray
) : (String) -> String {

    private lateinit var cipher: Lazy<Cipher>

    init {
        init()
    }

    protected fun invoke(input: ByteArray): ByteArray = cipher.value.doFinal(input)

    fun reset() {
        init()
    }

    private fun init() {
        cipher = lazy {
            Cipher.getInstance("AES/CBC/PKCS5Padding").apply { init(opMode, getKey(), IvParameterSpec(getIv())) }
        }
    }
}

private val charset = Charsets.UTF_8

internal class Encrypter(getKey: () -> Key, getIv: () -> ByteArray) : Cryptor(Cipher.ENCRYPT_MODE, getKey, getIv) {
    override fun invoke(input: String): String =
            Base64.encodeToString(invoke(input.toByteArray(charset)), Base64.DEFAULT)
}

internal class Decrypter(getKey: () -> Key, getIv: () -> ByteArray) : Cryptor(Cipher.DECRYPT_MODE, getKey, getIv) {
    override fun invoke(input: String) =
            invoke(Base64.decode(input, Base64.DEFAULT)).toString(charset)
}
