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

package ru.yoomoney.sdk.kassa.payments.userAuth

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginSchemeAuthSdk
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input

internal class MoneyAuthBusinessLogic(
    private val showState: suspend (MoneyAuth.State) -> MoneyAuth.Action,
    private val source: suspend () -> MoneyAuth.Action,
    private val tmxSessionIdStorage: TmxSessionIdStorage,
    private val currentUserRepository: CurrentUserRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val paymentOptionsListUseCase: PaymentOptionsListUseCase,
    private val getTransferDataUseCase: GetTransferDataUseCase,
    private val paymentParameters: PaymentParameters,
    private val loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
) : Logic<MoneyAuth.State, MoneyAuth.Action> {

    override fun invoke(
        state: MoneyAuth.State,
        action: MoneyAuth.Action
    ): Out<MoneyAuth.State, MoneyAuth.Action> {
        return when(state) {
            is MoneyAuth.State.Authorize -> when(action) {
                is MoneyAuth.Action.RequireAuth -> Out(state) {
                    input { showState(this.state) }
                }
                is MoneyAuth.Action.Authorized -> {
                    if (action.token != null) {
                        Out(MoneyAuth.State.CompleteAuth) {
                            input {
                                authorized(action)
                                showState(this.state)
                            }
                        }
                    } else {
                        Out(MoneyAuth.State.CancelAuth) {
                            input { MoneyAuth.Action.AuthCancelled }
                            input { showState(this.state) }
                        }
                    }
                }
                is MoneyAuth.Action.AuthCancelled -> Out(
                    MoneyAuth.State.WaitingForAuthStarted) {
                    input { showState(this.state) }
                }
                is MoneyAuth.Action.GetTransferData -> Out(state) {
                    input {
                        getTransferDataUseCase.getTransferAuxToken(action.cryptogram)
                    }
                }
                else -> Out.skip(state, source)
            }
            MoneyAuth.State.CompleteAuth -> when (action) {
                is MoneyAuth.Action.RequireAuth -> Out(
                    MoneyAuth.State.Authorize(getAuthStrategy(action.isYooMoneyCouldBeOpened))) {
                    input { showState(this.state) }
                }
                else -> Out.skip(state, source)
            }
            MoneyAuth.State.CancelAuth -> Out(MoneyAuth.State.WaitingForAuthStarted) {
                input { showState(this.state) }
            }
            MoneyAuth.State.WaitingForAuthStarted -> when (action) {
                is MoneyAuth.Action.RequireAuth -> Out(
                    MoneyAuth.State.Authorize(getAuthStrategy(action.isYooMoneyCouldBeOpened))) {
                    input { showState(this.state) }
                }
                else -> Out.skip(state, source)
            }
        }
    }

    private suspend fun authorized(action: MoneyAuth.Action.Authorized): MoneyAuth.Action {
        userAuthInfoRepository.userAuthToken = action.token
        userAuthInfoRepository.userAuthName = action.userAccount?.displayName?.title
        userAuthInfoRepository.userAvatarUrl = action.userAccount?.avatar?.url
        currentUserRepository.currentUser = AuthorizedUser()
        tmxSessionIdStorage.tmxSessionId = action.tmxSessionId
        loadedPaymentOptionListRepository.isActual = false
        paymentOptionsListUseCase.loadPaymentOptions(paymentParameters.amount)
        return MoneyAuth.Action.AuthSuccessful
    }

    private fun getAuthStrategy(isYooMoneyCouldBeOpened: Boolean): MoneyAuth.AuthorizeStrategy {
        return if (isYooMoneyCouldBeOpened) {
            MoneyAuth.AuthorizeStrategy.App2App
        } else {
            MoneyAuth.AuthorizeStrategy.InApp
        }
    }
}