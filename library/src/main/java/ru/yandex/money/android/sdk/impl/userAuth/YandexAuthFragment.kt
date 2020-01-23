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

package ru.yandex.money.android.sdk.impl.userAuth

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import com.yandex.authsdk.YandexAuthToken
import ru.yandex.money.android.sdk.impl.AppModel

private const val REQUEST_CODE_YANDEX_AUTH = 55

internal class YandexAuthFragment :
        Fragment(), YandexAuthorizeUserGateway.Listener {

    private var yandexAuthSdk: YandexAuthSdk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.also {
            yandexAuthSdk = YandexAuthSdk(it, YandexAuthOptions(it, true))
        }
        AppModel.yandexAuthGateway?.setListener(this)
    }

    override fun onDestroy() {
        AppModel.yandexAuthGateway?.removeListener()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_YANDEX_AUTH) {
            val token: YandexAuthToken? = try {
                yandexAuthSdk?.extractToken(resultCode, data)
            } catch (e: YandexAuthException) {
                null
            }

            if (token != null) {
                AppModel.yandexAuthGateway?.setResult(token)
            } else {
                AppModel.yandexAuthGateway?.cancel()
            }
        }
    }

    override fun collectCurrentUser() {
        context?.also { context ->
            yandexAuthSdk?.also {
                startActivityForResult(it.createLoginIntent(context, null), REQUEST_CODE_YANDEX_AUTH)
            }
        }
    }
}
