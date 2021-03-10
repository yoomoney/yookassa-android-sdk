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

package ru.yoo.sdk.kassa.payments.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import ru.yoo.sdk.kassa.payments.payment.PaymentOptionRepository
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoo.sdk.kassa.payments.secure.TokensStorage
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoo.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoo.sdk.kassa.payments.paymentOptionList.ApiV3PaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.paymentOptionList.InternetDependentRepository
import ru.yoo.sdk.kassa.payments.paymentOptionList.MockPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListErrorFormatter
import ru.yoo.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoo.sdk.kassa.payments.model.Fee
import ru.yoo.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.payment.InMemoryPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoo.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoo.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoo.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListAnalytics
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionsListBusinessLogic
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCaseImpl
import ru.yoo.sdk.march.Out
import ru.yoo.sdk.march.RuntimeViewModel
import ru.yoo.sdk.march.input
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
    fun paymentOptionListRepository(
        context: Context,
        httpClient: CheckoutOkHttpClient,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        tokensStorage: TokensStorage,
        errorReporter: ErrorReporter
    ): PaymentOptionListRepository {
        return if (testParameters.mockConfiguration != null) {
            MockPaymentOptionListRepository(
                testParameters.mockConfiguration.linkedCardsCount,
                Fee(service = testParameters.mockConfiguration.serviceFee)
            )
        } else {
            InternetDependentRepository(
                context,
                ApiV3PaymentOptionListRepository(
                    httpClient = lazy { httpClient },
                    gatewayId = paymentParameters.gatewayId,
                    tokensStorage = tokensStorage,
                    shopToken = paymentParameters.clientApplicationKey,
                    savePaymentMethod = paymentParameters.savePaymentMethod
                ),
                errorReporter
            )
        }
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
        paymentOptionRepository: PaymentOptionRepository,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): PaymentOptionsListUseCase {
        return PaymentOptionsListUseCaseImpl(
            paymentOptionListRestrictions = paymentParameters.paymentMethodTypes,
            paymentOptionListRepository = paymentOptionListRepository,
            saveLoadedPaymentOptionsListRepository = saveLoadedPaymentOptionsListRepository,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            currentUserRepository = currentUserRepository,
            googlePayRepository = googlePayRepository,
            paymentOptionRepository = paymentOptionRepository,
            loadedPaymentOptionListRepository = loadedPaymentOptionListRepository
        )
    }

    @[Provides IntoMap ViewModelKey(PAYMENT_OPTIONS)]
    fun viewModel(
        paymentOptionsListUseCase: PaymentOptionsListUseCase,
        paymentParameters: PaymentParameters,
        paymentMethodId: String?,
        reporter: Reporter,
        userAuthTypeParamProvider: UserAuthTypeParamProvider,
        tokenizeSchemeParamProvider: TokenizeSchemeParamProvider,
        logoutUseCase: LogoutUseCase
    ): ViewModel {
        return RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>(
            featureName = "PaymentOptionList",
            initial = {
                Out(PaymentOptionList.State.Loading) {
                    input { showState(state) }
                    input { paymentOptionsListUseCase.loadPaymentOptions(paymentParameters.amount, paymentMethodId) }
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
                        paymentParameters = paymentParameters
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