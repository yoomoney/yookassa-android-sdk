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
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ApiV3PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.InternetDependentRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.MockPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import javax.inject.Singleton

@Module
internal open class PaymentOptionsListModule {

    @Provides
    @Singleton
    open fun paymentOptionListRepository(
        context: Context,
        hostProvider: HostProvider,
        httpClient: CheckoutOkHttpClient,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        tokensStorage: TokensStorage,
        errorReporter: ErrorReporter,
        configRepository: ConfigRepository
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
                    hostProvider = hostProvider,
                    httpClient = lazy { httpClient },
                    gatewayId = paymentParameters.gatewayId,
                    tokensStorage = tokensStorage,
                    shopToken = paymentParameters.clientApplicationKey,
                    savePaymentMethod = paymentParameters.savePaymentMethod,
                    merchantCustomerId = paymentParameters.customerId,
                    configRepository = configRepository
                ),
                errorReporter
            )
        }
    }
}