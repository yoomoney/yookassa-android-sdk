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

package ru.yoo.sdk.kassa.payments.payment.googlePay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.CardRequirements
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import ru.yoo.sdk.kassa.payments.checkoutParameters.GooglePayParameters
import ru.yoo.sdk.kassa.payments.ui.PendingIntentActivity
import ru.yoo.sdk.kassa.payments.extensions.toWalletConstant
import ru.yoo.sdk.kassa.payments.logging.MsdkLogger
import ru.yoo.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoo.sdk.kassa.payments.model.GooglePayInfo
import ru.yoo.sdk.kassa.payments.model.SdkException
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

private const val GOOGLE_PAY_REQUEST_CODE = 0xAB1D

internal class GooglePayRepositoryImpl(
    context: Context,
    private val shopId: String,
    useTestEnvironment: Boolean,
    private val loadedPaymentOptionsRepository: GetLoadedPaymentOptionListRepository,
    private val googlePayParameters: GooglePayParameters,
    private val errorReporter: ErrorReporter
) : GooglePayRepository {

    private var paymentOptionId: Int? = null
    private var waitingForResult = false

    private val paymentsClient: PaymentsClient =
        Wallet.getPaymentsClient(
            context.applicationContext,
            Wallet.WalletOptions.Builder()
                .setEnvironment(
                    if (useTestEnvironment) {
                        WalletConstants.ENVIRONMENT_TEST
                    } else {
                        WalletConstants.ENVIRONMENT_PRODUCTION
                    }
                )
                .build()
        )

    override fun checkGooglePayAvailable(): Boolean {
        val request = IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build()
        val result = ArrayBlockingQueue<Boolean?>(1)
        paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
            try {
                result.offer(task.result)
            } catch (e: Exception) {
                val msg = """Google Pay payment option is disabled. If you want to use it, remove
                        |"<meta-data android:name="com.google.android.gms.wallet.api.enabled" tools:node="remove" />
                        |from your AndroidManifest. """.trimMargin()
                Log.d(MsdkLogger.TAG, msg)
                errorReporter.report(SdkException(msg))
                result.offer(false)
            }
        }
        return result.poll(10, TimeUnit.SECONDS) ?: false
    }

    override fun startGooglePayTokenize(fragment: Fragment, paymentOptionId: Int) {
        if (waitingForResult) {
            return
        }

        this.paymentOptionId = paymentOptionId

        val paymentOption = loadedPaymentOptionsRepository.getLoadedPaymentOptions().first { it.id == paymentOptionId }

        val request = PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice(paymentOption.charge.value.toPlainString())
                    .setCurrencyCode(paymentOption.charge.currency.currencyCode)
                    .build()
            )
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder().apply {
                    googlePayParameters.allowedCardNetworks.forEach {
                        addAllowedCardNetwork(it.toWalletConstant())
                    }
                    setAllowPrepaidCards(false)
                }.build()
            )
            .setPaymentMethodTokenizationParameters(
                PaymentMethodTokenizationParameters.newBuilder()
                    .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                    .addParameter("gateway", "yoomoney")
                    .addParameter("gatewayMerchantId", shopId)
                    .build()
            )
            .build()

        paymentsClient.loadPaymentData(request).addOnCompleteListener { task ->
            task.exception?.takeIf { it is ResolvableApiException }?.also {
                fragment.startActivityForResult(
                    PendingIntentActivity.createIntent(
                        fragment.requireContext(),
                        (it as ResolvableApiException).resolution
                    ), GOOGLE_PAY_REQUEST_CODE
                )
            }
        }
        waitingForResult = true
    }

    override fun handleGooglePayTokenize(requestCode: Int, resultCode: Int, data: Intent?): GooglePayTokenizationResult =
        if (requestCode == GOOGLE_PAY_REQUEST_CODE) {
            waitingForResult = false
            if (resultCode == Activity.RESULT_OK) {
                checkNotNull(PaymentData.getFromIntent(requireNotNull(data))).let {
                    GooglePayTokenizationSuccess(
                        paymentOptionId = checkNotNull(paymentOptionId),
                        paymentOptionInfo = GooglePayInfo(
                            paymentMethodToken = checkNotNull(it.paymentMethodToken).token,
                            googleTransactionId = it.googleTransactionId
                        )
                    )
                }
            } else {
                Log.d("GOOGLE_PAY_RESULT", data?.let { AutoResolveHelper.getStatusFromIntent(it) }?.statusMessage ?: "")
                GooglePayTokenizationCanceled()
            }
        } else {
            GooglePayNotHandled()
        }
}
