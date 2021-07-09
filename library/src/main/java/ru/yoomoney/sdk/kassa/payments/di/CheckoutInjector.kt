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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import com.yandex.metrica.YandexMetrica
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodId
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.utils.checkUrl
import ru.yoomoney.sdk.kassa.payments.extensions.initAppendableExtensions
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.metrics.ExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaExceptionReporter
import ru.yoomoney.sdk.kassa.payments.model.UnhandledException
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.ui.view.BankCardView
import ru.yoomoney.sdk.kassa.payments.utils.getAllPaymentMethods
import ru.yoomoney.sdk.kassa.payments.utils.isBuildDebug
import ru.yoomoney.sdk.march.Defaults

internal fun setComponent(
    context: Context,
    paymentParameters: PaymentParameters,
    testParameters: TestParameters,
    uiParameters: UiParameters,
    paymentMethodId: PaymentMethodId?,
    okHttpModule: OkHttpModule,
    tokensStorageModule: TokensStorageModule,
    currentUserModule: CurrentUserModule,
    yandexMetricaReporterModule: YandexMetricaReporterModule
) {
    CheckoutInjector.setupComponent(
        context = context,
        paymentParameters = paymentParameters,
        testParameters = testParameters,
        uiParameters = uiParameters,
        paymentMethodId = paymentMethodId,
        exceptionReporter = YandexMetricaExceptionReporter(YandexMetrica.getReporter(context, BuildConfig.APP_METRICA_KEY)),
        okHttpModule = okHttpModule,
        currentUserModule = currentUserModule,
        tokensStorageModule = tokensStorageModule,
        yandexMetricaReporterModule = yandexMetricaReporterModule
    )
}

internal object CheckoutInjector {

    private lateinit var component: CheckoutActivityComponent

    fun setupComponent(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        uiParameters: UiParameters,
        paymentMethodId: PaymentMethodId?,
        exceptionReporter: ExceptionReporter,
        okHttpModule: OkHttpModule = OkHttpModule(),
        yandexMetricaReporterModule: YandexMetricaReporterModule = YandexMetricaReporterModule(),
        currentUserModule: CurrentUserModule = CurrentUserModule(),
        tokensStorageModule: TokensStorageModule = TokensStorageModule()
    ) {

        val shopParameters =
            if (paymentParameters.paymentMethodTypes.isEmpty()) {
                paymentParameters.copy(paymentMethodTypes = getAllPaymentMethods())
            } else {
                paymentParameters
            }

        shopParameters.customReturnUrl?.let { checkUrl(it) }
        Defaults.isLoggingEnable = testParameters.showLogs && context.isBuildDebug()
        if (shopParameters.paymentMethodTypes.let { PaymentMethodType.YOO_MONEY in it }
            && shopParameters.authCenterClientId.isNullOrEmpty()) {
            val exception = IllegalStateException(
                "You should pass authCenterClientId to PaymentParameters if you want to allow PaymentMethodType.YOO_MONEY. " +
                        "If you don't want to use PaymentMethodType.YOO_MONEY, specify your payment methods " +
                        "explicitly in PaymentParameters.paymentMethodTypes \n" +
                        "Visit https://github.com/yoomoney/yookassa-android-sdk for more information."
            )
            exceptionReporter.report(UnhandledException(exception))
            throw exception
        }

        InMemoryColorSchemeRepository.colorScheme = uiParameters.colorScheme

        initAppendableExtensions(context)

        component = DaggerCheckoutActivityComponent.builder()
            .context(context.applicationContext)
            .paymentParameters(shopParameters)
            .testParameters(testParameters)
            .uiParameters(uiParameters)
            .paymentMethodId(paymentMethodId)
            .okHttpModule(okHttpModule)
            .mainReporterModule(yandexMetricaReporterModule)
            .tokensStorageModule(tokensStorageModule)
            .currentUserModule(currentUserModule)
            .build()
    }

    fun injectCheckoutActivity(activity: CheckoutActivity) {
        component.inject(activity)
    }

    fun injectMainDialogFragment(fragment: MainDialogFragment) {
        component.inject(fragment)
    }

    fun injectContractFragment(fragment: ContractFragment) {
        component.inject(fragment)
    }

    fun injectPaymentAuthFragment(fragment: PaymentAuthFragment) {
        component.inject(fragment)
    }

    fun injectPaymentOptionListFragment(fragment: PaymentOptionListFragment) {
        component.inject(fragment)
    }

    fun inject(fragment: MoneyAuthFragment) {
        component.inject(fragment)
    }

    fun inject(bankCardView: BankCardView) {
        component.inject(bankCardView)
    }
}