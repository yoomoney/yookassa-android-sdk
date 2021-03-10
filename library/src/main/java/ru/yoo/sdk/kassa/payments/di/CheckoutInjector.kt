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

package ru.yoo.sdk.kassa.payments.di

import android.util.Log
import ru.yoo.sdk.auth.YooMoneyAuth
import ru.yoo.sdk.kassa.payments.payment.PaymentMethodId
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoo.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoo.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoo.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoo.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoo.sdk.kassa.payments.utils.checkUrl
import ru.yoo.sdk.kassa.payments.extensions.initAppendableExtensions
import ru.yoo.sdk.kassa.payments.logging.MsdkLogger
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoo.sdk.kassa.payments.contract.ContractFragment
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.metrics.ExceptionReporter
import ru.yoo.sdk.kassa.payments.model.SdkException
import ru.yoo.sdk.kassa.payments.model.UnhandledException
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoo.sdk.kassa.payments.ui.view.BankCardView
import ru.yoo.sdk.kassa.payments.utils.getAllPaymentMethods

internal object CheckoutInjector {

    private lateinit var component: CheckoutActivityComponent

    fun injectCheckoutActivity(
        activity: CheckoutActivity,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        uiParameters: UiParameters,
        paymentMethodId: PaymentMethodId?,
        exceptionReporter: ExceptionReporter
    ) {

        val shopParameters =
            if (paymentParameters.paymentMethodTypes.isEmpty()) {
                paymentParameters.copy(paymentMethodTypes = getAllPaymentMethods())
            } else {
                paymentParameters
            }

        shopParameters.customReturnUrl?.let { checkUrl(it) }

        if (shopParameters.paymentMethodTypes.let { PaymentMethodType.YOO_MONEY in it }) {
            try {
                val moneyAuth = YooMoneyAuth::class.java
                if (testParameters.showLogs) {
                    Log.d(MsdkLogger.TAG, "YooMoney auth found: ${moneyAuth.canonicalName}")
                }
                if (shopParameters.authCenterClientId.isNullOrEmpty()) {
                    val exception = IllegalStateException(
                        "You should pass authCenterClientId to PaymentParameters if you want to allow PaymentMethodType.YOO_MONEY. " +
                                "If you don't want to use PaymentMethodType.YOO_MONEY, specify your payment methods " +
                                "explicitly in PaymentParameters.paymentMethodTypes \n" +
                                "Visit https://github.com/yoomoney/yookassa-android-sdk for more information."
                    )
                    exceptionReporter.report(UnhandledException(exception))
                    throw exception
                }
            } catch (e: NoClassDefFoundError) {
                val exception = IllegalStateException(
                    "You should add ru.yoo.sdk.auth:auth if you want to allow PaymentMethodType.YOO_MONEY. " +
                            "Check if you have ru.money.auth:auth in your dependencies and you pass authCenterClientId to PaymentParameters.\n" +
                            "If you don't want to use PaymentMethodType.YOO_MONEY, specify your payment methods " +
                            "explicitly in PaymentParameters.paymentMethodTypes \n" +
                            "Visit https://github.com/yoomoney/yookassa-android-sdk for more information."
                )
                exceptionReporter.report(UnhandledException(exception))
                throw exception
            }
        }

        InMemoryColorSchemeRepository.colorScheme = uiParameters.colorScheme

        initAppendableExtensions(activity)

        component = DaggerCheckoutActivityComponent.builder()
            .context(activity.applicationContext)
            .paymentParameters(shopParameters)
            .testParameters(testParameters)
            .uiParameters(uiParameters)
            .paymentMethodId(paymentMethodId)
            .build()
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