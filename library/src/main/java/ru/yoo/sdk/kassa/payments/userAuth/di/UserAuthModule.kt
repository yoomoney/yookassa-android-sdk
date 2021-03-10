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

package ru.yoo.sdk.kassa.payments.userAuth.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoo.sdk.kassa.payments.di.ViewModelKey
import ru.yoo.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoo.sdk.kassa.payments.secure.TokensStorage
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoo.sdk.kassa.payments.metrics.UserAuthTokenTypeParamProvider
import ru.yoo.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoo.sdk.kassa.payments.userAuth.MockAuthorizeUserRepository
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuth
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuthAnalytics
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuthBusinessLogic
import ru.yoo.sdk.kassa.payments.userAuth.YooMoneyAuthorizeUserRepository
import ru.yoo.sdk.kassa.payments.model.Executor
import ru.yoo.sdk.kassa.payments.model.Result
import ru.yoo.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoo.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoo.sdk.kassa.payments.userAuth.AuthorizeUserRepository
import ru.yoo.sdk.kassa.payments.userAuth.User
import ru.yoo.sdk.march.Out
import ru.yoo.sdk.march.RuntimeViewModel
import javax.inject.Singleton

@Module
internal class UserAuthModule {

    @Provides
    @Singleton
    fun authorizeUserRepository(
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        executor: Executor
    ): AuthorizeUserRepository {
        return when {
            testParameters.mockConfiguration != null -> MockAuthorizeUserRepository
            paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY) -> {
                YooMoneyAuthorizeUserRepository(
                    executor,
                    requireNotNull(paymentParameters.authCenterClientId)
                )
            }
            else -> {
                object : AuthorizeUserRepository {
                    override fun authorizeUser() = Result.Success(User.Empty)
                }
            }
        }
    }

    @Provides
    @Singleton
    fun checkPaymentAuthRequiredGateway(
        testParameters: TestParameters,
        tokensStorage: TokensStorage
    ): CheckPaymentAuthRequiredGateway {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null && mockConfiguration.paymentAuthPassed) {
            object : CheckPaymentAuthRequiredGateway {
                override fun checkPaymentAuthRequired() = false
            }
        } else {
            tokensStorage
        }
    }

    @Provides
    @Singleton
    fun tokenizeSchemeParamProvider(): TokenizeSchemeParamProvider {
        return TokenizeSchemeParamProvider()
    }

    @Provides
    @Singleton
    fun userAuthTypeParamProvider(
        currentUserRepository: CurrentUserRepository,
        checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway
    ): UserAuthTypeParamProvider {
        return UserAuthTypeParamProvider(
            currentUserRepository,
            checkPaymentAuthRequiredGateway
        )
    }

    @Provides
    @Singleton
    fun userAuthTokenTypeParamProvider(
        paymentAuthTokenRepository: PaymentAuthTokenRepository
    ): UserAuthTokenTypeParamProvider {
        return UserAuthTokenTypeParamProvider(
            paymentAuthTokenRepository
        )
    }

    @[Provides IntoMap ViewModelKey(MONEY_AUTH)]
    fun viewModel(
        reporter: Reporter,
        paymentParameters: PaymentParameters,
        tmxSessionIdStorage: TmxSessionIdStorage,
        currentUserRepository: CurrentUserRepository,
        tokensStorage: TokensStorage,
        useCase: PaymentOptionsListUseCase,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): ViewModel {
        val authCenterClientId = requireNotNull(paymentParameters.authCenterClientId)
        return RuntimeViewModel<MoneyAuth.State, MoneyAuth.Action, Unit>(
            featureName = MONEY_AUTH,
            initial = { Out.skip(MoneyAuth.State.WaitingForAuthStarted, source) },
            logic = {
                MoneyAuthAnalytics(
                    reporter = reporter,
                    businessLogic = MoneyAuthBusinessLogic(
                        showState = showState,
                        source = source,
                        authCenterClientId = authCenterClientId,
                        tmxSessionIdStorage = tmxSessionIdStorage,
                        currentUserRepository = currentUserRepository,
                        userAuthInfoRepository = tokensStorage,
                        paymentParameters = paymentParameters,
                        paymentOptionsListUseCase = useCase,
                        loadedPaymentOptionListRepository = loadedPaymentOptionListRepository
                    )
                )
            }
        )
    }

    companion object {
        const val MONEY_AUTH = "MoneyAuth"
    }
}