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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodRepository
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListErrorFormatter
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.InMemoryPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.payment.unbindCard.UnbindCardGateway
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ConfigUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ConfigUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListAnalytics
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListBusinessLogic
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepositoryImpl
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input
import javax.inject.Singleton

@Module
internal class PaymentOptionsModule {

    @Provides
    @PaymentOptionsListFormatter
    fun errorFormatter(
        context: Context,
        errorFormatter: ErrorFormatter
    ): ErrorFormatter {
        return PaymentOptionListErrorFormatter(context, errorFormatter)
    }

    @Provides
    @Singleton
    fun shopPropertiesRepository(): ShopPropertiesRepository {
        return ShopPropertiesRepositoryImpl()
    }

    @Provides
    @Singleton
    fun inMemoryPaymentOptionListRepository(): InMemoryPaymentOptionListRepository {
        return InMemoryPaymentOptionListRepository()
    }

    @Provides
    @Singleton
    fun saveLoadedPaymentOptionsListRepository(repository: InMemoryPaymentOptionListRepository): SaveLoadedPaymentOptionsListRepository {
        return repository
    }

    @Provides
    @Singleton
    fun loadedPaymentOptionsGateway(repository: InMemoryPaymentOptionListRepository): GetLoadedPaymentOptionListRepository {
        return repository
    }

    @Provides
    @Singleton
    fun paymentOptionsListUseCase(
        paymentParameters: PaymentParameters,
        paymentOptionListRepository: PaymentOptionListRepository,
        saveLoadedPaymentOptionsListRepository: SaveLoadedPaymentOptionsListRepository,
        paymentMethodInfoGateway: PaymentMethodInfoGateway,
        currentUserRepository: CurrentUserRepository,
        googlePayRepository: GooglePayRepository,
        paymentMethodRepository: PaymentMethodRepository,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        shopPropertiesRepository: ShopPropertiesRepository
    ): PaymentOptionsListUseCase {
        return PaymentOptionsListUseCaseImpl(
            paymentOptionListRestrictions = paymentParameters.paymentMethodTypes,
            paymentOptionListRepository = paymentOptionListRepository,
            saveLoadedPaymentOptionsListRepository = saveLoadedPaymentOptionsListRepository,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            currentUserRepository = currentUserRepository,
            googlePayRepository = googlePayRepository,
            paymentMethodRepository = paymentMethodRepository,
            loadedPaymentOptionListRepository = loadedPaymentOptionListRepository,
            shopPropertiesRepository = shopPropertiesRepository
        )
    }

    @Provides
    @Singleton
    fun unbindCardUseCaseProvider(
        unbindCardInfoGateway: UnbindCardGateway,
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): UnbindCardUseCase {
        return UnbindCardUseCaseImpl(unbindCardInfoGateway, getLoadedPaymentOptionListRepository)
    }

    @Provides
    @Singleton
    fun configUseCase(configRepository: ConfigRepository): ConfigUseCase {
        return ConfigUseCaseImpl(configRepository)
    }

    @[Provides IntoMap ViewModelKey(PAYMENT_OPTIONS)]
    fun viewModel(
        paymentOptionsListUseCase: PaymentOptionsListUseCase,
        paymentParameters: PaymentParameters,
        paymentMethodId: String?,
        reporter: Reporter,
        userAuthTypeParamProvider: UserAuthTypeParamProvider,
        tokenizeSchemeParamProvider: TokenizeSchemeParamProvider,
        logoutUseCase: LogoutUseCase,
        getConfirmation: GetConfirmation,
        unbindCardUseCase: UnbindCardUseCase,
        shopPropertiesRepository: ShopPropertiesRepository,
        configUseCase: ConfigUseCase,
        configRepository: ConfigRepository
    ): ViewModel {
        return RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>(
            featureName = "PaymentOptionList",
            initial = {
                Out(PaymentOptionList.State.Loading(configRepository.getConfig().yooMoneyLogoUrlLight)) {
                    input { showState(state) }
                    input { configUseCase.loadConfig() }
                }
            },
            logic = {
                PaymentOptionListAnalytics(
                    reporter = reporter,
                    businessLogic = PaymentOptionsListBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        useCase = paymentOptionsListUseCase,
                        logoutUseCase = logoutUseCase,
                        paymentParameters = paymentParameters,
                        paymentMethodId = paymentMethodId,
                        getConfirmation = getConfirmation,
                        unbindCardUseCase = unbindCardUseCase,
                        shopPropertiesRepository = shopPropertiesRepository,
                        configRepository = configRepository
                    ),
                    getUserAuthType = userAuthTypeParamProvider,
                    getTokenizeScheme = tokenizeSchemeParamProvider
                )
            }
        )
    }

    companion object {
        const val PAYMENT_OPTIONS = "PaymentOptionList"
    }
}