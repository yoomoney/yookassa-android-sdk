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

package ru.yoomoney.sdk.kassa.payments.contract

import ru.yoomoney.sdk.auth.Result
import ru.yoomoney.sdk.auth.account.AccountRepository
import ru.yoomoney.sdk.auth.account.model.UserAccount
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodRepository
import ru.yoomoney.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.selectOption.SelectedPaymentMethodOutputModel
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository

internal class SelectPaymentMethodUseCaseImpl(
    private val getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
    private val accountRepository: AccountRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : SelectPaymentMethodUseCase {

    override suspend fun select(): Contract.Action {
        val paymentOptionId = paymentMethodRepository.paymentOptionId ?: return Contract.Action.RestartProcess
        val instrumentId = paymentMethodRepository.instrumentId

        val paymentOptions = getLoadedPaymentOptionListRepository.getLoadedPaymentOptions()
        val option = paymentOptions.find { it.id == paymentOptionId } ?: return Contract.Action.LoadContractFailed(
            SelectedOptionNotFoundException(paymentOptionId)
        )

        val token = userAuthInfoRepository.userAuthToken

        if (token != null && option is Wallet) {
            when(val response = accountRepository.account(token)) {
                is Result.Success -> updateUserData(response.value)
            }
        }

        val walletLinkingPossible = option is YooMoney
                && checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()
                || option is LinkedCard

        val instrument = (option as? BankCardPaymentOption)?.paymentInstruments?.firstOrNull { it.paymentInstrumentId == instrumentId }

        return Contract.Action.LoadContractSuccess(
            SelectedPaymentMethodOutputModel(
                paymentOption = option,
                instrument = instrument,
                walletLinkingPossible = walletLinkingPossible
            )
        )
    }

    private fun updateUserData(userAccount: UserAccount) {
        userAuthInfoRepository.userAuthName = userAccount.displayName.title
        userAuthInfoRepository.userAvatarUrl = userAccount.avatar.url
    }
}