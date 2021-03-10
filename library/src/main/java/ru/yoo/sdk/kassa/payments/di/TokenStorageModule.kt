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

package ru.yoo.sdk.kassa.payments.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.secure.TokensStorage
import ru.yoo.sdk.kassa.payments.secure.BcKeyStorage
import ru.yoo.sdk.kassa.payments.secure.Decrypter
import ru.yoo.sdk.kassa.payments.secure.Encrypter
import ru.yoo.sdk.kassa.payments.secure.SharedPreferencesIvStorage
import ru.yoo.sdk.kassa.payments.secure.getPlatformPassword
import javax.inject.Singleton

@Module(includes = [SharedPreferencesModule::class])
internal class TokenStorageModule {

    @Provides
    @Singleton
    fun sharedPreferencesIvStorage(sharedPreferences: SharedPreferences): SharedPreferencesIvStorage {
        return SharedPreferencesIvStorage(sharedPreferences)
    }


    @Provides
    @Singleton
    fun keyStorage(context: Context, errorReporter: ErrorReporter): BcKeyStorage {
        return BcKeyStorage(context, "bc.keystore", getPlatformPassword(context), errorReporter)
    }

    @Provides
    @Singleton
    fun decrypter(ivStorage: SharedPreferencesIvStorage, keyStorage: BcKeyStorage): Decrypter {
        return Decrypter(
            getKey = { checkNotNull(keyStorage.get(keyKey)) },
            getIv = { checkNotNull(ivStorage.get(ivKey)) }
        )
    }

    @Provides
    @Singleton
    fun encrypter(ivStorage: SharedPreferencesIvStorage, keyStorage: BcKeyStorage): Encrypter {
        return Encrypter(
            getKey = { keyStorage.getOrCreate(keyKey) },
            getIv = { ivStorage.getOrCreate(ivKey) }
        )
    }

    @Provides
    @Singleton
    fun tokensStorage(sharedPreferences: SharedPreferences, encrypter: Encrypter, decrypter: Decrypter): TokensStorage {
        return TokensStorage(
            preferences = sharedPreferences,
            encrypt = encrypter,
            decrypt = decrypter
        )
    }

    companion object {
        val keyKey = "cipherKey"
        val ivKey = "cipherIv"
    }
}