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

package ru.yoo.sdk.kassa.payments.paymentAuth.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoo.sdk.kassa.payments.di.ViewModelKey
import ru.yoo.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.tmx.ProfilingTool
import ru.yoo.sdk.kassa.payments.tmx.TmxSessionIdStorage
import ru.yoo.sdk.kassa.payments.secure.TokensStorage
import ru.yoo.sdk.kassa.payments.metrics.Reporter
import ru.yoo.sdk.kassa.payments.paymentAuth.ApiV3PaymentAuthRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.MockPaymentAuthTypeRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.MockProcessPaymentAuthRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.MockSmsSessionRetryRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuth
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthAnalytics
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthBusinessLogic
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthUseCase
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthUseCaseImpl
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthUseCase
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthUseCaseImpl
import ru.yoo.sdk.kassa.payments.paymentAuth.SelectAppropriateAuthType
import ru.yoo.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTypeRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryRepository
import ru.yoo.sdk.march.Out
import ru.yoo.sdk.march.RuntimeViewModel
import ru.yoo.sdk.march.input
import javax.inject.Singleton

@Module
internal class PaymentAuthModule {

    @Provides
    @Singleton
    fun apiV3PaymentAuthRepository(
        httpClient: CheckoutOkHttpClient,
        tokensStorage: TokensStorage,
        paymentParameters: PaymentParameters,
        profilingTool: ProfilingTool,
        tmxSessionIdStorage: TmxSessionIdStorage
    ): ApiV3PaymentAuthRepository {
        return ApiV3PaymentAuthRepository(
            httpClient = lazy { httpClient },
            tokensStorage = tokensStorage,
            shopToken = paymentParameters.clientApplicationKey,
            tmxSessionIdStorage = tmxSessionIdStorage,
            profilingTool = profilingTool,
            selectAppropriateAuthType = SelectAppropriateAuthType()
        )
    }

    @Provides
    @Singleton
    fun paymentAuthTypeRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): PaymentAuthTypeRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockPaymentAuthTypeRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    @Singleton
    fun processPaymentAuthRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): ProcessPaymentAuthRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockProcessPaymentAuthRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    @Singleton
    fun paymentAuthTokenRepository(
        testParameters: TestParameters,
        tokensStorage: TokensStorage
    ): PaymentAuthTokenRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            object : PaymentAuthTokenRepository {
                override var paymentAuthToken: String? = "paymentAuthToken"
                override val isPaymentAuthPersisted: Boolean = true
                override fun persistPaymentAuth() {
                    // does nothing
                }
            }
        } else {
            tokensStorage
        }
    }

    @Provides
    @Singleton
    fun smsSessionRetryRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): SmsSessionRetryRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockSmsSessionRetryRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    fun requestPaymentAuthUseCase(paymentAuthTypeRepository: PaymentAuthTypeRepository): RequestPaymentAuthUseCase {
        return RequestPaymentAuthUseCaseImpl(
            paymentAuthTypeRepository
        )
    }

    @Provides
    fun processPaymentAuthUseCase(
        processPaymentAuthRepository: ProcessPaymentAuthRepository,
        currentUserRepository: CurrentUserRepository,
        paymentAuthTokenRepository: PaymentAuthTokenRepository,
        errorReporter: ErrorReporter
    ): ProcessPaymentAuthUseCase {
        return ProcessPaymentAuthUseCaseImpl(
            processPaymentAuthRepository,
            currentUserRepository,
            paymentAuthTokenRepository,
            errorReporter
        )
    }

    @[Provides IntoMap ViewModelKey(PAYMENT_AUTH)]
    fun viewModel(
        requestPaymentAuthUseCase: RequestPaymentAuthUseCase,
        processPaymentAuthUseCase: ProcessPaymentAuthUseCase,
        reporter: Reporter
    ): ViewModel {
        return RuntimeViewModel<PaymentAuth.State, PaymentAuth.Action, PaymentAuth.Effect>(
            featureName = "PaymentAuth",
            initial = {
                Out(PaymentAuth.State.Loading) {
                    input { showState(state) }
                }
            },
            logic = {
                PaymentAuthAnalytics(
                    reporter = reporter,
                    businessLogic = PaymentAuthBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        requestPaymentAuthUseCase = requestPaymentAuthUseCase,
                        processPaymentAuthUseCase = processPaymentAuthUseCase
                    )
                )
            }
        )
    }

    companion object {
        const val PAYMENT_AUTH = "PAYMENT_AUTH"
    }
}