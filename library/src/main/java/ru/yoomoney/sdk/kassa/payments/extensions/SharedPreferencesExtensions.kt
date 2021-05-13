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

@file:JvmName("SharedPreferencesExtensions")

package ru.yoomoney.sdk.kassa.payments.extensions

import android.content.SharedPreferences

internal inline fun SharedPreferences.edit(block: SharedPreferences.Editor.() -> Unit) {
    with(edit()) {
        block()
        apply()
    }
}

@Suppress("UNCHECKED_CAST")
internal operator fun <T : Any> SharedPreferences.set(key: String, value: T?) = edit {
    when (value) {
        is Boolean -> putBoolean(key, value)
        is Float -> putFloat(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Set<*> -> putStringSet(key, value as Set<String>)
        is String -> putString(key, value)
        null -> remove(key)
    }
}

internal inline operator fun <reified T : Any> SharedPreferences.get(key: String) = if (contains(key)) {
    when (T::class) {
        Boolean::class -> getBoolean(key, false) as T
        Float::class -> getFloat(key, 0f) as T
        Int::class -> getInt(key, 0) as T
        Long::class -> getLong(key, 0L) as T
        Set::class -> getStringSet(key, null) as T
        String::class -> getString(key, null) as T
        else -> throw IllegalArgumentException("type ${T::class.java.name} not allowed")
    }
} else {
    null
}

internal fun SharedPreferences.remove(key: String) {
    edit { remove(key) }
}