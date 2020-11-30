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

package ru.yoo.sdk.kassa.payments.impl.contract

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.SavePaymentMethod
import ru.yoo.sdk.kassa.payments.SavePaymentMethod.USER_SELECTS
import ru.yoo.sdk.kassa.payments.SavePaymentMethod.OFF
import ru.yoo.sdk.kassa.payments.SavePaymentMethod.ON
import ru.yoo.sdk.kassa.payments.impl.extensions.toHint
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentOptionPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.ProcessPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.RequestPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.SmsSessionRetryProgressViewModel
import ru.yoo.sdk.kassa.payments.model.AuthType
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.UserAuthRequired
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentAuthRequiredOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthSuccessOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthWrongAnswerOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryOutputModel
import ru.yoo.sdk.kassa.payments.utils.WebViewActivity
import ru.yoo.sdk.kassa.payments.utils.getMessageWithLink
import java.math.BigDecimal

internal class ContractPresenter(
    context: Context,
    private val shopTitle: CharSequence,
    private val shopSubtitle: CharSequence,
    private val requestedSavePaymentMethod: SavePaymentMethod,
    private val getSavePaymentMethodMessageLink: (PaymentOption) -> CharSequence,
    private val getSavePaymentMethodSwitchLink: (PaymentOption) -> CharSequence
) {
    private val context = context.applicationContext
    private val paymentOptionPresenter = PaymentOptionPresenter(context.applicationContext)

    private lateinit var contract: ContractSuccessViewModel
    private lateinit var paymentAuthForm: PaymentAuthFormViewModel

    operator fun invoke(model: SelectPaymentOptionOutputModel): ContractViewModel {

        return when (model) {
            is SelectedPaymentOptionOutputModel -> {
                val savePaymentMethodViewModel = if (model.paymentOption.savePaymentMethodAllowed) {
                    when (requestedSavePaymentMethod) {
                        ON -> SavePaymentMethodViewModel.On(getSavePaymentMethodMessageLink(model.paymentOption))
                        OFF -> SavePaymentMethodViewModel.UserSelects
                        USER_SELECTS -> SavePaymentMethodViewModel.Off(getSavePaymentMethodSwitchLink(model.paymentOption))
                    }
                } else {
                    SavePaymentMethodViewModel.UserSelects
                }

                when {
                    model.paymentOption is GooglePay -> {
                        val fee = model.paymentOption.fee?.service?.value
                        if (fee == null || fee == BigDecimal.ZERO) {
                            GooglePayContractViewModel(model.paymentOption.id)
                        } else {
                            ContractSuccessViewModel(
                                shopTitle = shopTitle,
                                shopSubtitle = shopSubtitle,
                                licenseAgreement = getLicenseAgreementText(),
                                paymentOption = paymentOptionPresenter(model.paymentOption),
                                showChangeButton = model.hasAnotherOptions,
                                savePaymentMethodViewModel = savePaymentMethodViewModel,
                                showAllowWalletLinking = model.walletLinkingPossible,
                                paymentAuth = null,
                                showPhoneInput = model.paymentOption is SbolSmsInvoicing,
                                googlePayContractViewModel = GooglePayContractViewModel(model.paymentOption.id)
                            ).also { contract = it }
                        }
                    }
                    else -> ContractSuccessViewModel(
                        shopTitle = shopTitle,
                        shopSubtitle = shopSubtitle,
                        licenseAgreement = getLicenseAgreementText(),
                        paymentOption = paymentOptionPresenter(model.paymentOption),
                        showChangeButton = model.hasAnotherOptions,
                        savePaymentMethodViewModel = savePaymentMethodViewModel,
                        showAllowWalletLinking = model.walletLinkingPossible,
                        paymentAuth = null,
                        showPhoneInput = model.paymentOption is SbolSmsInvoicing
                    ).also { contract = it }
                }
            }
            is UserAuthRequired -> ContractUserAuthRequiredViewModel
        }
    }

    private fun getLicenseAgreementText(): CharSequence {
        return getMessageWithLink(
            context,
            R.string.ym_license_agreement_part_1,
            R.string.ym_license_agreement_part_2
        ) {
            startActivity(
                context,
                WebViewActivity.create(
                    context,
                    context.getString(R.string.ym_license_agreement_url)
                )
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
            )
        }
    }

    operator fun invoke(model: TokenizeOutputModel) = when (model) {
        is TokenOutputModel -> ContractCompleteViewModel(
            token = model.token,
            type = when (model.option) {
                is YooMoney -> PaymentMethodType.YOO_MONEY
                is NewCard -> PaymentMethodType.BANK_CARD
                is GooglePay -> PaymentMethodType.GOOGLE_PAY
                is SbolSmsInvoicing -> PaymentMethodType.SBERBANK
                is PaymentIdCscConfirmation -> PaymentMethodType.BANK_CARD
            }
        )
        is TokenizePaymentAuthRequiredOutputModel -> contract.copy(
            showAllowWalletLinking = false,
            paymentAuth = PaymentAuthStartViewModel(model.charge)
        )
        else -> throw IllegalArgumentException("model not allowed: $model")
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun invoke(model: RequestPaymentAuthProgressViewModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = PaymentAuthProgressViewModel()
    )

    @Suppress("UNUSED_PARAMETER")
    operator fun invoke(model: ProcessPaymentAuthProgressViewModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = PaymentAuthProgressViewModel()
    )

    @Suppress("UNUSED_PARAMETER")
    operator fun invoke(model: SmsSessionRetryProgressViewModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = PaymentAuthProgressViewModel()
    )

    operator fun invoke(model: RequestPaymentAuthOutputModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = when (model.authTypeState.type) {
            AuthType.SMS, AuthType.PUSH -> PaymentAuthFormRetryViewModel(
                hint = model.authTypeState.type.toHint(context),
                timeout = model.authTypeState.nextSessionTimeLeft,
                error = null
            ).also { paymentAuthForm = it }
            AuthType.NOT_NEEDED -> AuthNotRequiredViewModel
            else -> PaymentAuthFormNoRetryViewModel(
                hint = model.authTypeState.type.toHint(context),
                error = null
            ).also { paymentAuthForm = it }
        }
    )

    operator fun invoke(model: ProcessPaymentAuthOutputModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = when (model) {
            is ProcessPaymentAuthSuccessOutputModel ->
                PaymentAuthSuccessViewModel()
            is ProcessPaymentAuthWrongAnswerOutputModel ->
                when (paymentAuthForm) {
                    is PaymentAuthFormRetryViewModel -> (paymentAuthForm as PaymentAuthFormRetryViewModel).copy(
                        error = context.getText(R.string.ym_wrong_passcode_error)
                    )
                    is PaymentAuthFormNoRetryViewModel -> (paymentAuthForm as PaymentAuthFormNoRetryViewModel).copy(
                        error = context.getText(R.string.ym_wrong_passcode_error)
                    )
                }
        }
    )

    operator fun invoke(model: SmsSessionRetryOutputModel) = contract.copy(
        showAllowWalletLinking = false,
        paymentAuth = PaymentAuthFormRetryViewModel(
            hint = model.authTypeState.type.toHint(context),
            timeout = model.authTypeState.nextSessionTimeLeft,
            error = null
        ).also { paymentAuthForm = it }
    )
}
