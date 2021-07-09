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
package ru.yoo.sdk.kassa.payments.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.tokenizeButton
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizationResult
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizeIntent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TOKENIZE) {
            when (resultCode) {
                Activity.RESULT_OK -> showToken(data)
                Activity.RESULT_CANCELED -> showError()
            }
        }
    }

    private fun initUi() {
        setContentView(R.layout.activity_main)
        tokenizeButton.setOnClickListener {
            onTokenizeButtonCLick()
        }
    }

    private fun onTokenizeButtonCLick() {
        val paymentMethodTypes = setOf(
            PaymentMethodType.GOOGLE_PAY,
            PaymentMethodType.BANK_CARD,
            PaymentMethodType.SBERBANK,
            PaymentMethodType.YOO_MONEY
        )
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.valueOf(10.0), Currency.getInstance("RUB")),
            title = getString(R.string.main_product_name),
            subtitle = getString(R.string.main_product_description),
            clientApplicationKey = BuildConfig.MERCHANT_TOKEN,
            shopId = BuildConfig.SHOP_ID,
            savePaymentMethod = SavePaymentMethod.OFF,
            paymentMethodTypes = paymentMethodTypes,
            gatewayId = BuildConfig.GATEWAY_ID,
            customReturnUrl = getString(R.string.test_redirect_url),
            userPhoneNumber = getString(R.string.test_phone_number),
            googlePayParameters = GooglePayParameters(),
            authCenterClientId = BuildConfig.CLIENT_ID
        )

        val intent = createTokenizeIntent(this, paymentParameters, TestParameters(showLogs = true))
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

    private fun showToken(data: Intent?) {
        if (data != null) {
            val token = createTokenizationResult(data).paymentToken
            Toast.makeText(
                this,
                String.format(Locale.getDefault(), getString(R.string.tokenization_success), token),
                Toast.LENGTH_LONG
            ).show()
        } else {
            showError()
        }
    }

    private fun showError() {
        Toast.makeText(this, R.string.tokenization_canceled, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_TOKENIZE = 1
    }
}