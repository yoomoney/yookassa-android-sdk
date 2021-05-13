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

package ru.yoomoney.sdk.kassa.payments.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.ConnectivityManager
import android.provider.Settings
import android.util.Base64
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.NoInternetException
import ru.yoomoney.sdk.kassa.payments.secure.sha256
import java.nio.ByteBuffer

/*
 * The MIT License
 *
 * Copyright (c) 2007—2017 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

internal fun Context.findDefaultLocalActivityForIntent(intent: Intent): ActivityInfo? {
    val applicationContext = this.applicationContext
    val applicationPackage = applicationContext.packageName
    val flags = PackageManager.GET_META_DATA
    return applicationContext.packageManager.queryIntentActivities(intent, flags)
        .asSequence()
        .map { it.activityInfo }
        .find { applicationPackage == it.packageName }
}

internal fun Context.checkConnection() {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return when (connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting) {
        true -> Unit
        else -> throw NoInternetException()
    }
}

@get:SuppressLint("HardwareIds")
internal val Context.androidId: String
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

@get:SuppressLint("PackageManagerGetSignatures")
internal val Context.appSignatures
    get() = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures ?: emptyArray()

internal val Context.appSignaturesFingerprint: ByteArray
    get() {
        val signaturesBytes = appSignatures.map(Signature::toByteArray)

        check(signaturesBytes.isNotEmpty())

        val fingerprint = signaturesBytes.fold(ByteBuffer.allocate(signaturesBytes.sumBy { it.size }), ByteBuffer::put)

        return sha256(fingerprint.array())
    }

internal val Context.appSignaturesFingerprintString: String
    get() = appSignaturesFingerprint
        .takeIf { it.isNotEmpty() }
        ?.let { Base64.encodeToString(it, Base64.DEFAULT) }
            ?: "unknown"

internal val Context.isTablet
    get() = resources.getBoolean(R.bool.ym_isTablet)
