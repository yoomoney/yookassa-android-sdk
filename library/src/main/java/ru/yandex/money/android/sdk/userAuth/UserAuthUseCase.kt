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

package ru.yandex.money.android.sdk.userAuth

import ru.yandex.money.android.sdk.AuthorizedUser
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.payment.CurrentUserGateway

internal class UserAuthUseCase(
        private val authorizeUserGateway: AuthorizeUserGateway,
        private val currentUserGateway: CurrentUserGateway,
        private val userAuthTokenGateway: UserAuthTokenGateway,
        private val walletCheckGateway: WalletCheckGateway
) : UseCase<UserAuthInputModel, UserAuthOutputModel> {

    override fun invoke(authInputModel: Unit): UserAuthOutputModel {
        val user = authorizeUserGateway.authorizeUser() ?: return UserAuthCancelledOutputModel()

        val hasWallet = walletCheckGateway.checkIfUserHasWallet(user.token)

        return if (hasWallet) {
            userAuthTokenGateway.userAuthToken = user.token
            val authorizedUser = AuthorizedUser(user.name).also(currentUserGateway::currentUser::set)

            UserAuthSuccessOutputModel(authorizedUser)
        } else {
            UserAuthNoWalletOutputModel(user.name)
        }
    }
}

internal typealias UserAuthInputModel = Unit

internal sealed class UserAuthOutputModel
internal data class UserAuthSuccessOutputModel(val authorizedUser: AuthorizedUser) : UserAuthOutputModel()
internal class UserAuthCancelledOutputModel : UserAuthOutputModel()
internal class UserAuthNoWalletOutputModel(val accountName: String) : UserAuthOutputModel()
