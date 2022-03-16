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

package ru.yoomoney.sdk.kassa.payments.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.yandex.metrica.YandexMetrica
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavedBankCardPaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.metrics.ColorMetricsProvider
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SavePaymentMethodProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAttiributionOnInitProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.YooKassaIconProvider
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import javax.inject.Inject

internal const val EXTRA_CREATED_WITH_CHECKOUT_METHOD = "ru.yoomoney.sdk.kassa.payments.extra.CREATED_WITH_CHECKOUT_METHOD"
internal const val EXTRA_PAYMENT_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_PARAMETERS"
internal const val EXTRA_CSC_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.CSC_PARAMETERS"
internal const val EXTRA_TEST_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.TEST_PARAMETERS"
internal const val EXTRA_UI_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.UI_PARAMETERS"

internal const val EXTRA_PAYMENT_TOKEN = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_TOKEN"

private val TAG_BOTTOM_SHEET = MainDialogFragment::class.java.name

private const val ACTION_SDK_INITIALIZED = "actionSDKInitialised"

internal class CheckoutActivity : AppCompatActivity() {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var reporter: Reporter

    @Inject
    lateinit var userAuthParamProvider: UserAuthTypeParamProvider

    @Inject
    lateinit var tokensStorage: TokensStorage

    private val paymentParameters: PaymentParameters by lazy {
        if (intent.hasExtra(EXTRA_PAYMENT_PARAMETERS)) {
            requireNotNull(intent.getParcelableExtra(EXTRA_PAYMENT_PARAMETERS))
        } else {
            val cscParameter =
                requireNotNull(intent.getParcelableExtra<SavedBankCardPaymentParameters>(EXTRA_CSC_PARAMETERS))
            PaymentParameters(
                amount = cscParameter.amount,
                title = cscParameter.title,
                subtitle = cscParameter.subtitle,
                clientApplicationKey = cscParameter.clientApplicationKey,
                shopId = cscParameter.shopId,
                gatewayId = cscParameter.gatewayId,
                paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD),
                savePaymentMethod = cscParameter.savePaymentMethod
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        checkStartedWithCreateTokenizeIntent()

        val paymentMethodId: String? = if (!intent.hasExtra(EXTRA_PAYMENT_PARAMETERS)) {
            requireNotNull(intent.getParcelableExtra<SavedBankCardPaymentParameters>(EXTRA_CSC_PARAMETERS)).paymentMethodId
        } else {
            null
        }
        val uiParameters: UiParameters = requireNotNull(intent.getParcelableExtra(EXTRA_UI_PARAMETERS))
        CheckoutInjector.setupComponent(
            this,
            paymentParameters,
            requireNotNull(intent.getParcelableExtra(EXTRA_TEST_PARAMETERS)),
            uiParameters,
            paymentMethodId,
            YandexMetricaExceptionReporter(YandexMetrica.getReporter(applicationContext, BuildConfig.APP_METRICA_KEY))
        )
        CheckoutInjector.injectCheckoutActivity(this)
        sendInitializeAction(paymentParameters, uiParameters)
        super.onCreate(savedInstanceState)

        showDialog(supportFragmentManager)
    }

    override fun onDestroy() {
        detachMainDialogFragment()
        super.onDestroy()
    }

    private fun detachMainDialogFragment() {
        findDialog(supportFragmentManager)?.dialog?.apply {
            setOnCancelListener(null)
            setOnDismissListener(null)
        }
    }

    private fun showDialog(supportFragmentManager: FragmentManager) {
        findDialog(supportFragmentManager) ?: MainDialogFragment()
            .show(supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    private fun findDialog(supportFragmentManager: FragmentManager) =
        supportFragmentManager.findFragmentByTag(TAG_BOTTOM_SHEET) as MainDialogFragment?

    private fun checkStartedWithCreateTokenizeIntent() {
        if (!intent.hasExtra(EXTRA_CREATED_WITH_CHECKOUT_METHOD)) {
            throw IllegalArgumentException(
                "Intent for CheckoutActivity should be created only with Checkout.createTokenizeIntent()."
            )
        }
    }

    private fun sendInitializeAction(paymentParameters: PaymentParameters, uiParameters: UiParameters) {
        reporter.report(
            ACTION_SDK_INITIALIZED,
            listOf(
                userAuthParamProvider.invoke(),
                SavePaymentMethodProvider().invoke(paymentParameters),
                YooKassaIconProvider().invoke(uiParameters),
                ColorMetricsProvider().invoke(uiParameters),
                UserAttiributionOnInitProvider(tokensStorage).invoke(paymentParameters)
            )
        )
    }
}