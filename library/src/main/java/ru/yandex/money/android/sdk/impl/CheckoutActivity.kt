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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import ru.yandex.money.android.sdk.SavedBankCardPaymentParameters
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.PaymentParameters
import ru.yandex.money.android.sdk.impl.contract.ContractCompleteViewModel
import ru.yandex.money.android.sdk.model.ViewModel

internal const val EXTRA_PAYMENT_PARAMETERS = "ru.yandex.money.android.extra.PAYMENT_PARAMETERS"
internal const val EXTRA_CSC_PARAMETERS = "ru.yandex.money.android.extra.CSC_PARAMETERS"
internal const val EXTRA_TEST_PARAMETERS = "ru.yandex.money.android.extra.TEST_PARAMETERS"
internal const val EXTRA_UI_PARAMETERS = "ru.yandex.money.android.extra.UI_PARAMETERS"
internal const val EXTRA_CREATED_WITH_CHECKOUT_METHOD = "ru.yandex.money.android.extra.CREATED_WITH_CHECKOUT_METHOD"

internal const val EXTRA_PAYMENT_TOKEN = "ru.yandex.money.android.extra.PAYMENT_TOKEN"
internal const val EXTRA_PAYMENT_METHOD_TYPE = "ru.yandex.money.android.extra.PAYMENT_METHOD_TYPE"

private val TAG_BOTTOM_SHEET = MainDialogFragment::class.java.name

internal class CheckoutActivity : AppCompatActivity() {

    private var showDialogListener: ((ViewModel) -> Unit)? = null
        set(value) {
            field?.also { AppModel.listeners -= it }
            field = value
            field?.also { AppModel.listeners += it }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            tokenize()
        }
        attachMainDialogFragment()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        detachMainDialogFragment()
        super.onDestroy()
    }

    private fun tokenize() {
        if (!intent.hasExtra(EXTRA_CREATED_WITH_CHECKOUT_METHOD)) {
            throw IllegalArgumentException(
                "Intent for CheckoutActivity should be created only with Checkout.createTokenizeIntent()."
            )
        }
        var paymentMethodId: String? = null
        val paymentParameter = if (intent.hasExtra(EXTRA_PAYMENT_PARAMETERS)) {
            intent.getParcelableExtra(EXTRA_PAYMENT_PARAMETERS) as PaymentParameters
        } else {
            val cscParameter = intent.getParcelableExtra(EXTRA_CSC_PARAMETERS) as SavedBankCardPaymentParameters
            paymentMethodId = cscParameter.paymentMethodId
            PaymentParameters(
                amount = cscParameter.amount,
                title = cscParameter.title,
                subtitle = cscParameter.subtitle,
                clientApplicationKey = cscParameter.clientApplicationKey,
                shopId = cscParameter.shopId,
                paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD)
            )
        }

        CheckoutInternal.tokenize(
            context = this,
            paymentParameters = paymentParameter,
            testParameters = intent.getParcelableExtra(EXTRA_TEST_PARAMETERS),
            uiParameters = intent.getParcelableExtra(EXTRA_UI_PARAMETERS),
            paymentMethodId = paymentMethodId
        )
    }

    private fun attachMainDialogFragment() {
        showDialogListener = {
            when (it) {
                is ContractCompleteViewModel -> AppModel.reset()
                else -> showDialog(supportFragmentManager)
            }
        }
        CheckoutInternal.onTokenReady(object : CheckoutInternal.OnTokenReadyListener {
            override fun onTokenReady(paymentToken: String, paymentMethodType: PaymentMethodType) {
                val result = Intent()
                    .putExtra(EXTRA_PAYMENT_TOKEN, paymentToken)
                    .putExtra(EXTRA_PAYMENT_METHOD_TYPE, paymentMethodType)
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        })
    }

    private fun detachMainDialogFragment() {
        showDialogListener = null
        CheckoutInternal.onTokenReady(null)
        findDialog(supportFragmentManager)?.dialog?.apply {
            setOnCancelListener(null)
            setOnDismissListener(null)
        }
    }

    private fun showDialog(supportFragmentManager: FragmentManager) {
        findDialog(supportFragmentManager) ?: MainDialogFragment().apply {
            show(supportFragmentManager, TAG_BOTTOM_SHEET)
            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun findDialog(supportFragmentManager: FragmentManager) =
        supportFragmentManager.findFragmentByTag(TAG_BOTTOM_SHEET) as MainDialogFragment?
}
