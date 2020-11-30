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

package ru.yoo.sdk.kassa.payments.impl

import android.content.Context
import android.util.Log
import ru.yoo.sdk.auth.YooMoneyAuth
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.PaymentParameters
import ru.yoo.sdk.kassa.payments.TestParameters
import ru.yoo.sdk.kassa.payments.UiParameters
import ru.yoo.sdk.kassa.payments.impl.contract.ContractCompleteViewModel
import ru.yoo.sdk.kassa.payments.impl.extensions.initExtensions
import ru.yoo.sdk.kassa.payments.impl.logging.MsdkLogger
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionAmountInputModel
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionPaymentMethodInputModel
import ru.yoo.sdk.kassa.payments.utils.WebViewActivity
import ru.yoo.sdk.kassa.payments.utils.getAllPaymentMethods

internal object CheckoutInternal {

    private var resultListener: ((ContractCompleteViewModel) -> Unit)? = null

    internal interface OnTokenReadyListener {
        fun onTokenReady(paymentToken: String, paymentMethodType: PaymentMethodType)
    }

    internal fun onTokenReady(onTokenReadyListener: OnTokenReadyListener?) {
        resultListener?.also { AppModel.listeners -= it }

        if (onTokenReadyListener != null) {
            val localResultListener: (ContractCompleteViewModel) -> Unit = {
                onTokenReadyListener.onTokenReady(it.token, it.type)
            }
            AppModel.listeners += localResultListener

            resultListener = localResultListener
        } else {
            resultListener = null
        }
    }

    internal fun checkUrl(url: String) {
        require(WebViewActivity.checkUrl(url)) { "Url $url is not allowed. It should be a valid https url." }
    }

    internal fun tokenize(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        uiParameters: UiParameters,
        paymentMethodId: String? = null
    ) {
        val shopParameters =
            if (paymentParameters.paymentMethodTypes.isEmpty()) {
                paymentParameters.copy(paymentMethodTypes = getAllPaymentMethods())
            } else {
                paymentParameters
            }

        shopParameters.customReturnUrl?.also {
            checkUrl(it)
        }

        if (shopParameters.paymentMethodTypes.let { PaymentMethodType.YOO_MONEY in it }) {
            try {
                val moneyAuth = YooMoneyAuth::class.java
                if (testParameters.showLogs) {
                    Log.d(MsdkLogger.TAG, "YooMoney auth found: ${moneyAuth.canonicalName}")
                }
                if (shopParameters.authCenterClientId.isNullOrEmpty()) {
                    throw IllegalStateException(
                        "You should pass authCenterClientId to PaymentParameters if you want to allow PaymentMethodType.YOO_MONEY. " +
                                "If you don't want to use PaymentMethodType.YOO_MONEY, specify your payment methods " +
                                "explicitly in PaymentParameters.paymentMethodTypes \n" +
                                "Visit https://github.com/yoomoney/yookassa-android-sdk for more information."
                    )
                }
            } catch (e: NoClassDefFoundError) {
                throw IllegalStateException(
                    "You should add ru.yoo.sdk.auth:auth if you want to allow PaymentMethodType.YOO_MONEY. " +
                            "Check if you have ru.money.auth:auth in your dependencies and you pass authCenterClientId to PaymentParameters.\n" +
                            "If you don't want to use PaymentMethodType.YOO_MONEY, specify your payment methods " +
                            "explicitly in PaymentParameters.paymentMethodTypes \n" +
                            "Visit https://github.com/yoomoney/yookassa-android-sdk for more information."
                )
            }
        }

        val appContext = context.applicationContext

        initExtensions(appContext)

        AppModel.init(
            argContext = appContext,
            paymentParameters = shopParameters,
            testParameters = testParameters,
            uiParameters = uiParameters
        )
        val inputModel = if (paymentMethodId == null) {
            PaymentOptionAmountInputModel(paymentParameters.amount)
        } else {
            PaymentOptionPaymentMethodInputModel(paymentParameters.amount, paymentMethodId)
        }
        AppModel.loadPaymentOptionListController(inputModel)
    }
}
