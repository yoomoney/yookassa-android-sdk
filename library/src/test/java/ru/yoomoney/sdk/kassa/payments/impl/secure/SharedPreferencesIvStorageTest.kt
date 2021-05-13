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

package ru.yoomoney.sdk.kassa.payments.impl.secure

import android.content.Context
import android.util.Base64
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull.notNullValue
import org.hamcrest.core.IsNull.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.extensions.edit
import ru.yoomoney.sdk.kassa.payments.extensions.set
import ru.yoomoney.sdk.kassa.payments.secure.SharedPreferencesIvStorage
import java.util.Arrays
import java.util.concurrent.TimeUnit

private const val KEY = "key"

@RunWith(RobolectricTestRunner::class)
class SharedPreferencesIvStorageTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val sp = RuntimeEnvironment.application.getSharedPreferences("sp", Context.MODE_PRIVATE).apply {
        edit { clear() }
    }
    private val ivStorage = SharedPreferencesIvStorage(sp)

    @Test
    fun getShouldReturnNullIfNothingWrote() {
        // prepare

        // invoke
        val bytes = ivStorage.get(KEY)

        // assert
        assertThat(bytes, nullValue())
    }

    @Test
    fun getShouldReturnByteArrayIfWrote() {
        //
        // prepare
        val value = ByteArray(6, Int::toByte)
        sp[KEY] = Base64.encodeToString(value, Base64.DEFAULT)

        // invoke
        val bytes = ivStorage.get(KEY)

        // assert
        assertThat("should equals", Arrays.equals(value, bytes))
    }

    @Test
    fun getOrCreateShouldCreateAndReturnValueIfNothingWrote() {
        // prepare

        // invoke
        val bytes = ivStorage.getOrCreate(KEY)

        // assert
        assertThat(bytes, notNullValue())
    }

    @Test
    fun getOrCreateShouldReturnValueIfWrote() {
        // prepare
        val value = ByteArray(6, Int::toByte)
        sp[KEY] = Base64.encodeToString(value, Base64.DEFAULT)

        // invoke
        val bytes = ivStorage.getOrCreate(KEY)

        // assert
        assertThat("should equals", Arrays.equals(value, bytes))
    }

    @Test
    fun removeShouldRemoveValue() {
        // prepare
        val value = ByteArray(6, Int::toByte)
        sp[KEY] = Base64.encodeToString(value, Base64.DEFAULT)

        // invoke
        ivStorage.remove(KEY)
        val bytes = ivStorage.get(KEY)


        // assert
        assertThat(bytes, nullValue())
    }
}
