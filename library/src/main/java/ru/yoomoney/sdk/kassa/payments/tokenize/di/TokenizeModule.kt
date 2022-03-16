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

package ru.yoomoney.sdk.kassa.payments.tokenize.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.tokenize.Tokenize
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeAnalytics
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeBusinessLogic
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeUseCase
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeUseCaseImpl
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

@Module
internal class TokenizeModule {

    @Provides
    fun tokenizeUseCase(
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        tokenizeRepository: TokenizeRepository,
        checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
        paymenPaymentAuthTokenRepository: PaymentAuthTokenRepository
    ): TokenizeUseCase {
        return TokenizeUseCaseImpl(
            getLoadedPaymentOptionListRepository,
            tokenizeRepository,
            checkPaymentAuthRequiredGateway,
            paymenPaymentAuthTokenRepository = paymenPaymentAuthTokenRepository
        )
    }

    @[Provides IntoMap ViewModelKey(TOKENIZE)]
    fun viewModel(
        tokenizeUseCase: TokenizeUseCase,
        reporter: Reporter,
        errorScreenReporter: ErrorScreenReporter,
        paymentParameters: PaymentParameters,
        uiParameters: UiParameters,
        tokensStorage: TokensStorage,
        userAuthTypeParamProvider: UserAuthTypeParamProvider,
        tokenizeSchemeParamProvider: TokenizeSchemeParamProvider
    ): ViewModel {
        return RuntimeViewModel<Tokenize.State, Tokenize.Action, Tokenize.Effect>(
            featureName = "Tokenize",
            initial = {
                Out(Tokenize.State.Start) {
                    input { showState(state) }
                }
            },
            logic = {
                TokenizeAnalytics(
                    reporter = reporter,
                    errorScreenReporter = errorScreenReporter,
                    businessLogic = TokenizeBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        tokenizeUseCase = tokenizeUseCase
                    ),
                    getUserAuthType = userAuthTypeParamProvider,
                    userAuthInfoRepository = tokensStorage,
                    getTokenizeScheme = tokenizeSchemeParamProvider,
                    paymentParameters = paymentParameters,
                    uiParameters = uiParameters
                )
            }
        )
    }

    companion object {
        const val TOKENIZE = "TOKENIZE"
    }
}