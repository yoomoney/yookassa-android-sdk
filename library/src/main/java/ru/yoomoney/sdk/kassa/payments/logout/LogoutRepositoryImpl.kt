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

package ru.yoomoney.sdk.kassa.payments.logout

import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.model.AnonymousUser
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository

internal class LogoutRepositoryImpl(
    private val currentUserRepository: CurrentUserRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val paymentAuthTokenRepository: PaymentAuthTokenRepository,
    private val loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val tmxSessionIdStorage: TmxSessionIdStorage,
    private val removeKeys: () -> Unit,
    private val revokeUserAuthToken: (token: String?) -> Unit
) : LogoutRepository {

    override suspend fun logout() {
        val token = userAuthInfoRepository.userAuthToken
        userAuthInfoRepository.userAuthToken = null
        userAuthInfoRepository.userAuthName = null
        userAuthInfoRepository.passportAuthToken = null
        paymentAuthTokenRepository.paymentAuthToken = null
        tmxSessionIdStorage.tmxSessionId = null
        currentUserRepository.currentUser = AnonymousUser
        removeKeys()
        loadedPaymentOptionListRepository.isActual = false
        revokeUserAuthToken(token)
    }
}
