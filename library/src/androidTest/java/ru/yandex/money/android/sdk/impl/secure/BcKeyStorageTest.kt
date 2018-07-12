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

package ru.yandex.money.android.sdk.impl.secure

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull.notNullValue
import org.hamcrest.core.IsNull.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.io.FileNotFoundException
import java.security.KeyStore
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator

private const val KEY = "key"
private const val PATH = "bc.keystore"
private const val PASSWORD = "123456"
private const val KEYSTORE_TYPE = "BouncyCastle"

@RunWith(AndroidJUnit4::class)
class BcKeyStorageTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)


    private val context = InstrumentationRegistry.getContext()
    private val storage = KeyStore.getInstance(KEYSTORE_TYPE).apply {
        try {
            context.openFileInput(PATH).use { file -> load(file, PASSWORD.toCharArray()) }
        } catch (e: FileNotFoundException) {
            load(null)
        }
    }
    private lateinit var keyStorage: BcKeyStorage

    @Before
    fun setUp() {
        keyStorage = BcKeyStorage(context, PATH, PASSWORD)
    }

    @Test
    fun getShouldReturnNullIfNothingWrote() {
        // prepare

        // invoke
        val bytes = keyStorage.get(KEY)

        // assert
        assertThat(bytes, nullValue())
    }

    @Test
    fun getShouldReturnByteArrayIfWrote() {
        // prepare
        val key = KeyGenerator.getInstance("AES", "BC").generateKey()
        storage.setKeyEntry(KEY, key, null, null)
        context.openFileOutput(PATH, Context.MODE_PRIVATE).use { file ->
            storage.store(file, PASSWORD.toCharArray())
        }
        keyStorage = BcKeyStorage(context, PATH, PASSWORD)

        // invoke
        val bytes = keyStorage.get(KEY)!!.encoded

        // assert
        assertThat("should equals", Arrays.equals(key.encoded, bytes))
    }

    @Test
    fun getOrCreateShouldCreateAndReturnValueIfNothingWrote() {
        // prepare

        // invoke
        val bytes = keyStorage.getOrCreate(KEY)

        // assert
        assertThat(bytes, notNullValue())
    }

    @Test
    fun getOrCreateShouldReturnValueIfWrote() {
        // prepare
        val key = KeyGenerator.getInstance("AES", "BC").generateKey()
        storage.setKeyEntry(KEY, key, null, null)
        context.openFileOutput(PATH, Context.MODE_PRIVATE).use { file ->
            storage.store(file, PASSWORD.toCharArray())
        }
        keyStorage = BcKeyStorage(context, PATH, PASSWORD)

        // invoke
        val bytes = keyStorage.getOrCreate(KEY).encoded

        // assert
        assertThat("should equals", Arrays.equals(key.encoded, bytes))
    }

    @Test
    fun removeShouldRemoveValue() {
        // prepare
        val value = ByteArray(6, Int::toByte)
        storage.setKeyEntry(KEY, value, null)

        // invoke
        keyStorage.remove(KEY)
        val bytes = keyStorage.get(KEY)


        // assert
        assertThat(bytes, nullValue())
    }
}
