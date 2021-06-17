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
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaExceptionReporter
import javax.inject.Inject

internal const val EXTRA_CREATED_WITH_CHECKOUT_METHOD = "ru.yoomoney.sdk.kassa.payments.extra.CREATED_WITH_CHECKOUT_METHOD"
internal const val EXTRA_PAYMENT_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_PARAMETERS"
internal const val EXTRA_CSC_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.CSC_PARAMETERS"
internal const val EXTRA_TEST_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.TEST_PARAMETERS"
internal const val EXTRA_UI_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.UI_PARAMETERS"

internal const val EXTRA_PAYMENT_TOKEN = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_TOKEN"

private val TAG_BOTTOM_SHEET = MainDialogFragment::class.java.name

internal class CheckoutActivity : AppCompatActivity() {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    private val paymentParameters: PaymentParameters by lazy {
        if (intent.hasExtra(EXTRA_PAYMENT_PARAMETERS)) {
            intent.getParcelableExtra(EXTRA_PAYMENT_PARAMETERS) as PaymentParameters
        } else {
            val cscParameter = intent.getParcelableExtra(EXTRA_CSC_PARAMETERS) as SavedBankCardPaymentParameters
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
            (intent.getParcelableExtra(EXTRA_CSC_PARAMETERS) as SavedBankCardPaymentParameters).paymentMethodId
        } else {
            null
        }
        CheckoutInjector.setupComponent(
            this,
            paymentParameters,
            intent.getParcelableExtra(EXTRA_TEST_PARAMETERS),
            intent.getParcelableExtra(EXTRA_UI_PARAMETERS),
            paymentMethodId,
            YandexMetricaExceptionReporter(YandexMetrica.getReporter(applicationContext, BuildConfig.APP_METRICA_KEY))
        )
        CheckoutInjector.injectCheckoutActivity(this)
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
}
