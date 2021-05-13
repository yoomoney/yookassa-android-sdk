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

import android.content.Context
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import java.io.FileNotFoundException
import java.security.Key
import java.security.KeyStore
import javax.crypto.KeyGenerator

private const val KEYSTORE_TYPE = "BouncyCastle"

internal class BcKeyStorage(
    private val context: Context,
    private val path: String,
    password: String,
    errorReporter: ErrorReporter
) : Storage<Key> {

    private val password = password.toCharArray()
    private val store = loadKeyStore(context, path, this.password, errorReporter)

    override fun get(key: String): Key? = store.getKey(key, null)

    override fun getOrCreate(key: String): Key = get(key) ?: generateKey().also { generatedKey ->
        store.setKeyEntry(key, generatedKey, null, null)
        saveStore()
    }

    override fun remove(key: String) {
        store.deleteEntry(key)
        saveStore()
    }

    private fun saveStore() {
        context.openFileOutput(path, Context.MODE_PRIVATE).use { file ->
            store.store(file, password)
        }
    }
}

private fun loadKeyStore(context: Context, path: String, password: CharArray, errorReporter: ErrorReporter): KeyStore {
    return KeyStore.getInstance(KEYSTORE_TYPE).apply {
        try {
            context.openFileInput(path).use { file -> load(file, password) }
        } catch (e: FileNotFoundException) {
            errorReporter.report(SdkException(e))
            load(null)
        }
    }
}

private fun generateKey() = KeyGenerator.getInstance("AES").generateKey()
