/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.secure.Decrypter
import ru.yoomoney.sdk.kassa.payments.secure.Encrypter
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import javax.inject.Singleton


@Module
internal open class TokensStorageModule {

    @Provides
    @Singleton
    open fun tokensStorage(sharedPreferences: SharedPreferences, encrypter: Encrypter, decrypter: Decrypter): TokensStorage {
        return TokensStorage(
            preferences = sharedPreferences,
            encrypt = encrypter,
            decrypt = decrypter
        )
    }
}