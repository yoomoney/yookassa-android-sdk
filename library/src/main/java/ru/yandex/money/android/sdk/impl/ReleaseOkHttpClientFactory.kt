@file:JvmName("ReleaseOkHttpClientFactory")
package ru.yandex.money.android.sdk.impl

import android.content.Context
import okhttp3.OkHttpClient

@Suppress("UNUSED_PARAMETER")
internal fun OkHttpClient.Builder.applyLogging() = this
