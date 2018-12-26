/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.Keep
import android.support.v4.app.FragmentManager
import android.util.Log
import com.yandex.authsdk.YandexAuthSdk
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.MainDialogFragment
import ru.yandex.money.android.sdk.impl.contract.ContractCompleteViewModel
import ru.yandex.money.android.sdk.impl.extensions.initExtensions
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.EXTRA_CARD_NUMBER
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.EXTRA_EXPIRY_MONTH
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.EXTRA_EXPIRY_YEAR
import ru.yandex.money.android.sdk.utils.CheckoutConfirmationActivity
import ru.yandex.money.android.sdk.utils.EXTRA_REDIRECT_URL
import ru.yandex.money.android.sdk.utils.EXTRA_URL
import java.net.URL

/**
 * Entry point for MSDK. All actions related to MSDK should be performed through this class.
 */
object Checkout {

    @Keep
    const val EXTRA_ERROR_CODE = "ru.yandex.money.android.extra.ERROR_CODE"
    @Keep
    const val EXTRA_ERROR_DESCRIPTION = "ru.yandex.money.android.extra.ERROR_DESCRIPTION"
    @Keep
    const val EXTRA_ERROR_FAILING_URL = "ru.yandex.money.android.extra.ERROR_FAILING_URL"
    @Keep
    const val RESULT_ERROR = Activity.RESULT_FIRST_USER
    @Keep
    const val ERROR_NOT_HTTPS_URL = Int.MIN_VALUE

    private val TAG_BOTTOM_SHEET = MainDialogFragment::class.java.name

    private var shopParameters: ShopParameters? = null
    private var configuration: Configuration? = null

    private var showDialogListener: ((ViewModel) -> Unit)? = null
        set(value) {
            field?.also { AppModel.listeners -= it }
            field = value
            field?.also { AppModel.listeners += it }
        }
    private var resultListener: ((ContractCompleteViewModel) -> Unit)? = null

    @[JvmStatic Keep]
    fun create3dsIntent(context: Context, url: URL, redirectUrl: URL): Intent =
        Intent(context, CheckoutConfirmationActivity::class.java)
            .putExtra(EXTRA_URL, url)
            .putExtra(EXTRA_REDIRECT_URL, redirectUrl)

    /**
     * Create scan result intent for bank card scanning
     * [cardNumber] - card number, may be separated with spaces, should be no more than 23 symbols length
     * [expirationMonth] - card expiration month, should be between 1 and 12
     * [expirationYear] - card expiration year, should be last 2 digits of year
     */
    @[JvmStatic Keep]
    fun createScanBankCardResult(cardNumber: String, expirationMonth: Int, expirationYear: Int): Intent {
        require(cardNumber.length <= 23) { "cardNumber should be no more than 23 symbols length" }
        require(expirationMonth in 1..12) { "expirationMonth should be between 1 and 12" }
        require(expirationYear in 0..99) { "expirationYear should be last 2 digits of year" }

        return Intent()
            .putExtra(EXTRA_CARD_NUMBER, cardNumber)
            .putExtra(EXTRA_EXPIRY_MONTH, expirationMonth)
            .putExtra(EXTRA_EXPIRY_YEAR, expirationYear)
    }

    private fun init(context: Context, shopParameters: ShopParameters, configuration: Configuration?) {
        this.shopParameters = shopParameters

        if (shopParameters.paymentMethodTypes.let { it.isEmpty() || PaymentMethodType.YANDEX_MONEY in it }) {
            try {
                val yandexAuth = YandexAuthSdk::class.java
                Log.d("MSDK", "yandex auth found: ${yandexAuth.canonicalName}")
            } catch (e: NoClassDefFoundError) {
                throw IllegalStateException(
                    "You should add Yandex Login SDK if you want to allow PaymentMethodType.YANDEX_MONEY. " +
                            "Visit https://tech.yandex.ru/mobileauthsdk/ for more information."
                )
            }
        }

        val appContext = context.applicationContext

        initExtensions(appContext)

        AppModel.init(appContext, shopParameters, configuration)
    }

    /**
     * Configure test mode parameters using [Configuration]
     */
    @[JvmStatic Keep]
    fun configureTestMode(configuration: Configuration?) {
        this.configuration = configuration
    }

    /**
     * Start tokenize process with given parameters. Result will be returned to the resultCallback
     * that should be specified in setResultCallback().
     *
     * @param context application context
     * @param amount payment amount, see ru.yandex.money.android.sdk.Amount
     * @param shopParameters parameters for shop, see ru.yandex.money.android.sdk.ShopParameters
     */
    @[JvmStatic Suppress("DEPRECATION") Keep]
    fun tokenize(context: Context, amount: Amount, shopParameters: ShopParameters) {
        init(context, shopParameters, configuration)
        AppModel.loadPaymentOptionListController(amount)
    }

    /**
     * Set [resultCallback] that will be invoked after successful tokenize
     */
    @[JvmStatic Keep]
    fun setResultCallback(resultCallback: ResultCallback?) {
        resultListener?.also { AppModel.listeners -= it }

        if (resultCallback != null) {
            val localResultListener: (ContractCompleteViewModel) -> Unit = {
                resultCallback.onResult(it.token, it.type)
            }
            AppModel.listeners += localResultListener

            this.resultListener = localResultListener
        } else {
            resultListener = null
        }
    }

    /**
     * Attach Checkout to [supportFragmentManager] to show UI
     */
    @[JvmStatic Keep]
    fun attach(supportFragmentManager: FragmentManager) {
        this.showDialogListener = {
            when (it) {
                is ContractCompleteViewModel -> AppModel.reset()
                else -> showDialog(supportFragmentManager)
            }
        }
    }

    /**
     * Detach Checkout from supportFragmentManager
     */
    @[JvmStatic Keep]
    fun detach() {
        showDialogListener = null
    }

    private fun showDialog(supportFragmentManager: FragmentManager) {
        if (findDialog(supportFragmentManager) == null) {
            MainDialogFragment().show(supportFragmentManager, TAG_BOTTOM_SHEET)
            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun findDialog(supportFragmentManager: FragmentManager) =
        supportFragmentManager.findFragmentByTag(TAG_BOTTOM_SHEET) as MainDialogFragment?

    /**
     * Callback that will be invoked after successful tokenize
     */
    interface ResultCallback {
        /**
         * Method that will be invoked after successful tokenize
         * @param paymentToken token to proceed payment
         * @param type selected payment method type
         */
        @Keep
        fun onResult(paymentToken: String, type: PaymentMethodType)
    }
}
