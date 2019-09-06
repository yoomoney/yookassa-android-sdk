/*
 * The MIT License (MIT)
 * Copyright © 2019 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.impl

import android.content.Context
import android.util.Log
import com.yandex.authsdk.YandexAuthSdk
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.PaymentParameters
import ru.yandex.money.android.sdk.TestParameters
import ru.yandex.money.android.sdk.UiParameters
import ru.yandex.money.android.sdk.impl.contract.ContractCompleteViewModel
import ru.yandex.money.android.sdk.impl.extensions.initExtensions
import ru.yandex.money.android.sdk.impl.logging.MsdkLogger
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionAmountInputModel
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionPaymentMethodInputModel
import ru.yandex.money.android.sdk.utils.WebViewActivity
import ru.yandex.money.android.sdk.utils.getAllPaymentMethods

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

        if (shopParameters.paymentMethodTypes.let { PaymentMethodType.YANDEX_MONEY in it }) {
            try {
                val yandexAuth = YandexAuthSdk::class.java
                if (testParameters.showLogs) {
                    Log.d(MsdkLogger.TAG, "yandex auth found: ${yandexAuth.canonicalName}")
                }
            } catch (e: NoClassDefFoundError) {
                throw IllegalStateException(
                    "You should add Yandex Login SDK if you want to allow PaymentMethodType.YANDEX_MONEY. " +
                            "Check if you have com.yandex.android:authsdk in your dependencies " +
                            "and YANDEX_CLIENT_ID as your manifest placeholder. \n" +
                            "If you don't want to use PaymentMethodType.YANDEX_MONEY, specify your payment methods" +
                            "explicitly in PaymentParameters.paymentMethodTypes \n" +
                            "Visit https://github.com/yandex-money/yandex-checkout-android-sdk for more information."
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
