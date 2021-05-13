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

package ru.yoomoney.sdk.kassa.payments.contract.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.auth.YooMoneyAuth
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.payment.PaymentOptionRepository
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.TokenStorageModule
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.kassa.payments.utils.DEFAULT_REDIRECT_URL
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingTool
import ru.yoomoney.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.contract.Contract
import ru.yoomoney.sdk.kassa.payments.contract.ContractAnalytics
import ru.yoomoney.sdk.kassa.payments.contract.ContractBusinessLogic
import ru.yoomoney.sdk.kassa.payments.contract.SelectPaymentOptionUseCase
import ru.yoomoney.sdk.kassa.payments.contract.SelectPaymentOptionUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.contract.TokenizeUseCase
import ru.yoomoney.sdk.kassa.payments.contract.TokenizeUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.extensions.getConfirmation
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepositoryImpl
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorScreenReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTokenTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.ApiV3TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.MockTokenizeRepository
import ru.yoomoney.sdk.kassa.payments.secure.BcKeyStorage
import ru.yoomoney.sdk.kassa.payments.secure.Decrypter
import ru.yoomoney.sdk.kassa.payments.secure.Encrypter
import ru.yoomoney.sdk.kassa.payments.secure.SharedPreferencesIvStorage
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepository
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

@Module
internal class ContractModule {

    @Provides
    fun provideTokenizeRepository(
        testParameters: TestParameters,
        httpClient: CheckoutOkHttpClient,
        tokensStorage: TokensStorage,
        paymentParameters: PaymentParameters,
        profilingTool: ProfilingTool,
        tmxSessionIdStorage: TmxSessionIdStorage
    ): TokenizeRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockTokenizeRepository(mockConfiguration.completeWithError)
        } else {
            ApiV3TokenizeRepository(
                httpClient = lazy { httpClient },
                shopToken = paymentParameters.clientApplicationKey,
                paymentAuthTokenRepository = tokensStorage,
                profilingTool = profilingTool,
                tmxSessionIdStorage = tmxSessionIdStorage
            )
        }
    }

    @Provides
    fun selectPaymentOptionsUseCase(
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
        paymentOptionRepository: PaymentOptionRepository
    ): SelectPaymentOptionUseCase {
        return SelectPaymentOptionUseCaseImpl(
            getLoadedPaymentOptionListRepository,
            checkPaymentAuthRequiredGateway,
            paymentOptionRepository = paymentOptionRepository
        )
    }

    @Provides
    fun tokenizeUseCase(
        paymentParameters: PaymentParameters,
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        tokenizeRepository: TokenizeRepository,
        checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
        paymenPaymentAuthTokenRepository: PaymentAuthTokenRepository
    ): TokenizeUseCase {
        return TokenizeUseCaseImpl(
            getLoadedPaymentOptionListRepository,
            tokenizeRepository,
            checkPaymentAuthRequiredGateway,
            paymenPaymentAuthTokenRepository = paymenPaymentAuthTokenRepository,
            getConfirmation = {
                it.getConfirmation(paymentParameters.customReturnUrl ?: DEFAULT_REDIRECT_URL)
            }
        )
    }

    @Provides
    fun provideLogoutRepository(
        context: Context,
        currentUserRepository: CurrentUserRepository,
        userAuthInfoRepository: TokensStorage,
        paymentAuthTokenRepository: PaymentAuthTokenRepository,
        tmxSessionIdStorage: TmxSessionIdStorage,
        paymentParameters: PaymentParameters,
        ivStorage: SharedPreferencesIvStorage,
        encrypt: Encrypter,
        keyStorage: BcKeyStorage,
        decrypt: Decrypter,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): LogoutRepository {
        return LogoutRepositoryImpl(
            currentUserRepository = currentUserRepository,
            userAuthInfoRepository = userAuthInfoRepository,
            paymentAuthTokenRepository = paymentAuthTokenRepository,
            tmxSessionIdStorage = tmxSessionIdStorage,
            removeKeys = {
                ivStorage.remove(TokenStorageModule.ivKey)
                keyStorage.remove(TokenStorageModule.keyKey)
                encrypt.reset()
                decrypt.reset()
            },
            revokeUserAuthToken = { token ->
                if (token != null && paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY)) {
                    YooMoneyAuth.logout(context.applicationContext, token)
                }
            },
            loadedPaymentOptionListRepository = loadedPaymentOptionListRepository
        )
    }

    @Provides
    fun logoutUseCase(
        logoutRepository: LogoutRepository
    ): LogoutUseCase {
        return LogoutUseCaseImpl(logoutRepository)
    }

    @[Provides IntoMap ViewModelKey(CONTRACT)]
    fun viewModel(
        selectPaymentOptionUseCase: SelectPaymentOptionUseCase,
        tokenizeUseCase: TokenizeUseCase,
        paymentParameters: PaymentParameters,
        logoutUseCase: LogoutUseCase,
        reporter: Reporter,
        errorScreenReporter: ErrorScreenReporter,
        userAuthTypeParamProvider: UserAuthTypeParamProvider,
        userAuthTokenTypeParamProvider: UserAuthTokenTypeParamProvider
    ): ViewModel {
        return RuntimeViewModel<Contract.State, Contract.Action, Contract.Effect>(
            featureName = "Contract",
            initial = {
                Out(Contract.State.Loading) {
                    input { showState(state) }
                    input { selectPaymentOptionUseCase.select() }
                }
            },
            logic = {
                ContractAnalytics(
                    reporter = reporter,
                    errorScreenReporter = errorScreenReporter,
                    businessLogic = ContractBusinessLogic(
                        paymentParameters = paymentParameters,
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        selectPaymentOptionUseCase = selectPaymentOptionUseCase,
                        tokenizeUseCase = tokenizeUseCase,
                        logoutUseCase = logoutUseCase
                    ),
                    getUserAuthType = userAuthTypeParamProvider,
                    getUserAuthTokenType = userAuthTokenTypeParamProvider
                )
            }
        )
    }

    companion object {
        const val CONTRACT = "CONTRACT"
    }
}