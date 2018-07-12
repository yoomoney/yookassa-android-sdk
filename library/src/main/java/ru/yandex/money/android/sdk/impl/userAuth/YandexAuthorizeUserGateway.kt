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

import android.content.Context
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import com.yandex.authsdk.YandexAuthToken
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.Executor
import ru.yandex.money.android.sdk.impl.extensions.getDisplayName
import ru.yandex.money.android.sdk.impl.extensions.getJsonPayload
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthorizeUserGateway.State.COLLECTING_USER
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthorizeUserGateway.State.HAS_TO_COLLECT_USER
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthorizeUserGateway.State.IDLE
import ru.yandex.money.android.sdk.userAuth.AuthorizeUserGateway
import java.util.concurrent.Semaphore

internal class YandexAuthorizeUserGateway(
        private val uiExecutor: Executor,
        private val context: Context
) : AuthorizeUserGateway {

    private var listener: Listener? = null

    private val semaphore = Semaphore(0)
    private var token: YandexAuthToken? = null
    private var state: State = IDLE

    override fun authorizeUser(): AuthorizeUserGateway.User? {
        state = HAS_TO_COLLECT_USER
        notifyListener()
        semaphore.acquire()

        return token?.let {
            val sdk = YandexAuthSdk(context, YandexAuthOptions(context, true))
            AuthorizeUserGateway.User(
                    name = sdk.getJwt(it).getJsonPayload().getDisplayName(),
                    token = BuildConfig.DEFAULT_YANDEX_AUTH_TOKEN ?: it.value)
        }
    }

    fun setResult(token: YandexAuthToken) {
        this.token = token
        state = IDLE
        semaphore.release()
    }

    fun cancel() {
        state = IDLE
        token = null
        semaphore.release()
    }

    fun reset() {
        state = IDLE
        token = null
        semaphore.drainPermits()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
        if (state == HAS_TO_COLLECT_USER) {
            collectUser(listener)
        }
    }

    fun removeListener() {
        this.listener = null
    }

    private fun notifyListener() {
        listener.takeIf { state == HAS_TO_COLLECT_USER }?.also {
            collectUser(it)
        }
    }

    private fun collectUser(currentListener: Listener) {
        uiExecutor {
            currentListener.collectCurrentUser()
        }
        state = COLLECTING_USER
    }

    internal interface Listener {
        fun collectCurrentUser()
    }

    enum class State {
        HAS_TO_COLLECT_USER,
        COLLECTING_USER,
        IDLE
    }
}
