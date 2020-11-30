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

package ru.yoo.sdk.kassa.payments.impl.userAuth

import ru.yoo.sdk.auth.account.model.UserAccount
import ru.yoo.sdk.kassa.payments.BuildConfig
import ru.yoo.sdk.kassa.payments.impl.userAuth.YooMoneyAuthorizeUserGateway.State.COLLECTING_USER
import ru.yoo.sdk.kassa.payments.impl.userAuth.YooMoneyAuthorizeUserGateway.State.HAS_TO_COLLECT_USER
import ru.yoo.sdk.kassa.payments.impl.userAuth.YooMoneyAuthorizeUserGateway.State.IDLE
import ru.yoo.sdk.kassa.payments.model.Executor
import ru.yoo.sdk.kassa.payments.userAuth.AuthorizeUserGateway
import java.util.concurrent.Semaphore

internal class YooMoneyAuthorizeUserGateway(
    private val uiExecutor: Executor,
    private val authCenterClientId: String
) : AuthorizeUserGateway {

    private var listener: Listener? = null

    private val semaphore = Semaphore(0)
    private var token: String? = null
    private var userAccount: UserAccount? = null
    private var state: State = IDLE

    override fun authorizeUser(): AuthorizeUserGateway.User? {
        state = HAS_TO_COLLECT_USER
        notifyListener()
        semaphore.acquire()

        return token?.let { AuthorizeUserGateway.User(token = it) }
    }

    fun setResult(token: String, userAccount: UserAccount?) {
        this.token = token
        this.userAccount = userAccount
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
            currentListener.collectCurrentUser(authCenterClientId)
        }
        state = COLLECTING_USER
    }

    internal interface Listener {
        fun collectCurrentUser(authCenterClientId: String)
    }

    enum class State {
        HAS_TO_COLLECT_USER,
        COLLECTING_USER,
        IDLE
    }
}
