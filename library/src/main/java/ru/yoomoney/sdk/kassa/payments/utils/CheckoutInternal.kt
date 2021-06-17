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

package ru.yoomoney.sdk.kassa.payments.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import ru.yoomoney.sdk.kassa.payments.impl.SBERBANK_ONLINE_PACKAGE

// Do not forget to change redirect link in test.html at /assets
internal const val DEFAULT_REDIRECT_URL = "checkoutsdk://success"
internal const val INVOICING_AUTHORITY = "invoicing"
internal const val SBERPAY_PATH = "sberpay"

internal fun checkUrl(url: String) {
    require(WebViewActivity.checkUrl(url)) { "Url $url is not allowed. It should be a valid https url." }
}

internal fun Context.canResolveIntent(intent: Intent): Boolean {
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
}

internal fun createSberbankIntent(context: Context?, confirmationUrl: String): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(confirmationUrl)).apply {
        context?.packageManager?.queryIntentActivities(this, 0)
            ?.firstOrNull { it.activityInfo.applicationInfo.packageName.contains(SBERBANK_ONLINE_PACKAGE) }
            ?.run { component = ComponentName(activityInfo.packageName, activityInfo.name) }
    }
}

internal fun isSberBankAppInstalled(context: Context): Boolean {
    return try {
        val packageManager = context.packageManager
        packageManager.getPackageInfo(SBERBANK_ONLINE_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}