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

package ru.yoomoney.sdk.kassa.payments.userAuth.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.auth.YooMoneyAuth
import ru.yoomoney.sdk.auth.account.AccountRepository
import ru.yoomoney.sdk.auth.transferData.TransferDataRepository
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTokenTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.model.Executor
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.userAuth.AuthorizeUserRepository
import ru.yoomoney.sdk.kassa.payments.userAuth.GetTransferDataUseCase
import ru.yoomoney.sdk.kassa.payments.userAuth.GetTransferDataUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.userAuth.MockAuthorizeUserRepository
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthAnalytics
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthBusinessLogic
import ru.yoomoney.sdk.kassa.payments.userAuth.User
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuth
import ru.yoomoney.sdk.kassa.payments.userAuth.YooMoneyAuthorizeUserRepository
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
                override fun checkUserAccountRemember() = true
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
        return UserAuthTokenTypeParamProvider(paymentAuthTokenRepository)
    }

    @Provides
    @Singleton
    fun transferDataRepository(
        context: Context,
        paymentParameters: PaymentParameters,
        hostProvider: HostProvider
    ): TransferDataRepository {
        return YooMoneyAuth.provideTransferDataRepository(
            context = context,
            authCenterClientId = requireNotNull(paymentParameters.authCenterClientId),
            apiHost = if (!hostProvider.authHost().isNullOrEmpty()) hostProvider.authHost() else null,
            isDebugMode = !hostProvider.authHost().isNullOrEmpty()
        )
    }

    @Provides
    @Singleton
    fun getTransferDataUseCase(transferDataRepository: TransferDataRepository): GetTransferDataUseCase {
        return GetTransferDataUseCaseImpl(transferDataRepository)
    }

    @Provides
    @Singleton
    fun accountRepository(context: Context, hostProvider: HostProvider): AccountRepository {
        return YooMoneyAuth.provideAccountRepository(
            context = context,
            apiHost = if (!hostProvider.authHost().isNullOrEmpty()) hostProvider.authHost() else null,
            isDebugMode = !hostProvider.authHost().isNullOrEmpty()
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
        getTransferDataUseCase: GetTransferDataUseCase,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): ViewModel {
        return RuntimeViewModel<MoneyAuth.State, MoneyAuth.Action, Unit>(
            featureName = MONEY_AUTH,
            initial = { Out.skip(MoneyAuth.State.WaitingForAuthStarted, source) },
            logic = {
                MoneyAuthAnalytics(
                    reporter = reporter,
                    businessLogic = MoneyAuthBusinessLogic(
                        showState = showState,
                        source = source,
                        tmxSessionIdStorage = tmxSessionIdStorage,
                        currentUserRepository = currentUserRepository,
                        userAuthInfoRepository = tokensStorage,
                        paymentParameters = paymentParameters,
                        paymentOptionsListUseCase = useCase,
                        getTransferDataUseCase = getTransferDataUseCase,
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