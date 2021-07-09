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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yandex.metrica.YandexMetrica
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.logging.ReporterLogger
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SberPayConfirmationStatusSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaReporter
import ru.yoomoney.sdk.kassa.payments.utils.INVOICING_AUTHORITY
import ru.yoomoney.sdk.kassa.payments.utils.SBERPAY_PATH
import ru.yoomoney.sdk.kassa.payments.utils.createSberbankIntent
import ru.yoomoney.sdk.kassa.payments.utils.getSberbankPackage

internal const val EXTRA_PAYMENT_METHOD_TYPE = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_METHOD_TYPE"
internal const val EXTRA_CONFIRMATION_URL = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_SBER_CONFIRMATION_URL"

private const val SBER_PAY_CONFIRMATION_ACTION = "actionSberPayConfirmation"

internal class ConfirmationActivity : AppCompatActivity() {

    private var isWaitingForResult = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private lateinit var reporter: Reporter

    override fun onCreate(savedInstanceState: Bundle?) {
        reporter = ReporterLogger(
            YandexMetricaReporter(
                YandexMetrica.getReporter(applicationContext, BuildConfig.APP_METRICA_KEY)
            )
        )
        val confirmationUrl = intent.getStringExtra(EXTRA_CONFIRMATION_URL)
        if (confirmationUrl != null) {
            super.onCreate(savedInstanceState)
            startConfirmationProcess(
                confirmationUrl,
                intent.getSerializableExtra(EXTRA_PAYMENT_METHOD_TYPE) as PaymentMethodType,
                intent.getParcelableExtra(EXTRA_TEST_PARAMETERS) as TestParameters
            )
            return
        } else {
            handleIntent(intent)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (isWaitingForResult) {
            isWaitingForResult = false
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            isWaitingForResult = true
        }
    }

    private fun startConfirmationProcess(confirmationUrl: String, paymentMethodType: PaymentMethodType, testParameters: TestParameters) {
        when (paymentMethodType) {
            PaymentMethodType.SBERBANK -> startActivity(createSberbankIntent(this, confirmationUrl, getSberbankPackage(testParameters.hostParameters.isDevHost)))
            else -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun handleIntent(newIntent: Intent) {
        if (newIntent.data?.authority == INVOICING_AUTHORITY) {
            if (newIntent.data?.path?.contains(SBERPAY_PATH) == true) {
                setResult(Activity.RESULT_OK)
                reporter.report(SBER_PAY_CONFIRMATION_ACTION, listOf(SberPayConfirmationStatusSuccess()))
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        isWaitingForResult = false
        finish()
    }
}