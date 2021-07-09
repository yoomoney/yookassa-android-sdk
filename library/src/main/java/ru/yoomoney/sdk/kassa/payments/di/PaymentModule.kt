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
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.payment.SharedPreferencesCurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.paymentMethodInfo.ApiV3PaymentMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.paymentMethodInfo.MockPaymentInfoGateway
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepositoryImpl
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.MockGooglePayRepository
import ru.yoomoney.sdk.kassa.payments.model.AuthorizedUser
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import javax.inject.Singleton

@Module(includes = [PaymentOptionsModule::class])
internal class PaymentModule {

    @Provides
    fun paymentMethodInfoGateway(
        hostProvider: HostProvider,
        okHttpClient: CheckoutOkHttpClient,
        tokensStorage: TokensStorage,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters
    ): PaymentMethodInfoGateway {
        return if (testParameters.mockConfiguration != null) {
            MockPaymentInfoGateway()
        } else {
            ApiV3PaymentMethodInfoGateway(
                hostProvider = hostProvider,
                httpClient = lazy { okHttpClient },
                tokensStorage = tokensStorage,
                shopToken = paymentParameters.clientApplicationKey
            )
        }
    }

    @Provides
    @Singleton
    fun googlePayRepository(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        errorReporter: ErrorReporter
    ): GooglePayRepository {
        return if (testParameters.mockConfiguration != null) {
            MockGooglePayRepository(true)
        } else {
            GooglePayRepositoryImpl(
                context = context,
                shopId = paymentParameters.shopId,
                useTestEnvironment = testParameters.googlePayTestEnvironment,
                loadedPaymentOptionsRepository = getLoadedPaymentOptionListRepository,
                googlePayParameters = paymentParameters.googlePayParameters,
                errorReporter = errorReporter
            )
        }
    }
}