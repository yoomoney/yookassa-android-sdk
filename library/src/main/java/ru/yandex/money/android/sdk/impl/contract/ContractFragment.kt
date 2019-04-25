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

package ru.yandex.money.android.sdk.impl.contract

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.ym_fragment_contract.*
import kotlinx.android.synthetic.main.ym_item_common.*
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.extensions.configureForPhoneInput
import ru.yandex.money.android.sdk.impl.extensions.hideSoftKeyboard
import ru.yandex.money.android.sdk.impl.extensions.isPhoneNumber
import ru.yandex.money.android.sdk.impl.extensions.showChild
import ru.yandex.money.android.sdk.impl.extensions.showSoftKeyboard
import ru.yandex.money.android.sdk.impl.extensions.visible
import ru.yandex.money.android.sdk.impl.paymentAuth.PaymentAuthView
import ru.yandex.money.android.sdk.impl.paymentOptionList.GooglePayNotHandled
import ru.yandex.money.android.sdk.impl.paymentOptionList.GooglePayTokenizationCanceled
import ru.yandex.money.android.sdk.impl.paymentOptionList.GooglePayTokenizationSuccess
import ru.yandex.money.android.sdk.model.SbolSmsInvoicingInfo
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionInputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeInputModel
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthInputModel
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthInputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthInputModel
import ru.yandex.money.android.sdk.utils.showLogoutDialog

internal class ContractFragment : Fragment() {

    private val showContractProgress: (ContractProgressViewModel) -> Unit = {
        if (isAdded) {
            rootContainer.showChild(loadingView)
        }
    }

    private val showContract: (ContractSuccessViewModel) -> Unit = { viewModel ->
        if (isAdded) {
            rootContainer.showChild(contentView)

            title.text = viewModel.shopTitle
            subtitle.text = viewModel.shopSubtitle

            with(viewModel.paymentOption) {
                primaryText.text = name

                secondaryText.text = additionalInfo
                secondaryText.visible = additionalInfo != null
                // suddenly, secondaryText not shown at first time, so we need this hack
                secondaryText.parent.requestLayout()

                if (canLogout) {
                    primaryText.setOnClickListener {
                        showLogoutDialog(primaryText.context, name)
                    }
                }

                image.setImageDrawable(icon)
                sum.text = amount

                nextButton.setOnClickListener { _ ->
                    AppModel.tokenizeController(TokenizeInputModel(optionId, viewModel.showAllowRecurringPayments))
                }
            }

            endText.takeIf { viewModel.showChangeButton }?.apply {
                text = context.getString(R.string.ym_contract_change_payment_option)
                setTextColor(ContextCompat.getColor(context, R.color.ym_button_text_link))
                setOnClickListener {
                    hideSoftKeyboard()
                    AppModel.changePaymentOptionController(Unit)
                }
            }

            switchesAndPaymentAuthContainer.visible = viewModel.paymentAuth != null
                    || viewModel.showAllowWalletLinking
                    || viewModel.showAllowRecurringPayments
                    || viewModel.showPhoneInput

            when (viewModel.paymentAuth) {
                null -> {
                    if (viewModel.showPhoneInput) {
                        switchesAndPaymentAuthContainer.showChild(additionalInfoInputViewContainer)
                        additionalInfoInputViewContainer.showChild(phoneInput)
                        phoneInput.apply {
                            requestFocus()
                            showSoftKeyboard()
                        }

                        nextButton.setOnClickListener { _ ->
                            val text = phoneInput.text
                            if (text != null && text.isPhoneNumber) {
                                AppModel.tokenizeController(
                                    TokenizeInputModel(
                                        paymentOptionId = viewModel.paymentOption.optionId,
                                        allowRecurringPayments = viewModel.showAllowRecurringPayments,
                                        paymentOptionInfo = SbolSmsInvoicingInfo(text.toString())
                                    )
                                )
                            } else {
                                phoneInputContainer.error = " "
                            }
                        }
                    } else {
                        switchesAndPaymentAuthContainer.showChild(switchesContainer)

                        allowWalletLinkingContainer.visible = viewModel.showAllowWalletLinking
                        allowWalletLinking.text =
                            allowWalletLinking.context.getString(R.string.ym_allow_wallet_linking)

                        allowRecurringPaymentsContainer.visible = viewModel.showAllowRecurringPayments
                    }
                }
                is PaymentAuthStartViewModel -> {
                    AppModel.requestPaymentAuthController(
                        RequestPaymentAuthInputModel(
                            linkWalletToApp = allowWalletLinking.isChecked,
                            amount = viewModel.paymentAuth.amount
                        )
                    )
                }
                is PaymentAuthFormViewModel -> {
                    switchesAndPaymentAuthContainer.showChild(additionalInfoInputViewContainer)
                    additionalInfoInputViewContainer.showChild(paymentAuth)

                    val processPaymentAuth = {
                        val accessCode = paymentAuth.getAccessCode()

                        if (accessCode.isEmpty()) {
                            paymentAuth.error = " "
                        } else {
                            AppModel.processPaymentAuthController(
                                ProcessPaymentAuthInputModel(
                                    passphrase = accessCode,
                                    saveAuth = allowWalletLinking.isChecked
                                )
                            )
                        }
                    }

                    with(paymentAuth) {
                        setViewModel(
                            when (viewModel.paymentAuth) {
                                is PaymentAuthFormRetryViewModel -> PaymentAuthView.Model.Retry(
                                    accessCodeHint = viewModel.paymentAuth.hint,
                                    doneAction = processPaymentAuth,
                                    retryAction = { AppModel.smsSessionRetryController(Unit) },
                                    retryTime = viewModel.paymentAuth.timeout

                                )
                                is PaymentAuthFormNoRetryViewModel -> PaymentAuthView.Model.NoRetry(
                                    accessCodeHint = viewModel.paymentAuth.hint,
                                    doneAction = processPaymentAuth
                                )
                            }
                        )
                        error = viewModel.paymentAuth.error ?: ""
                        requestFocusAndShowSoftKeyboard()
                    }
                    nextButton.setOnClickListener {
                        processPaymentAuth()
                    }
                }
                is PaymentAuthRestartViewModel -> AppModel.requestPaymentAuthController.retry()
                is PaymentAuthShowUserAuthViewModel -> AppModel.userAuthController(UserAuthInputModel)
                is PaymentAuthProgressViewModel -> {
                    switchesAndPaymentAuthContainer.showChild(additionalInfoInputViewContainer)
                    additionalInfoInputViewContainer.showChild(paymentAuthLoading)
                }
                is PaymentAuthSuccessViewModel -> AppModel.tokenizeController.retry()
            }
        }
    }

    private val restartProcess: (ContractRestartProcessViewModel) -> Unit = {
        AppModel.loadPaymentOptionListController.retry()
    }

    private val userAuthRequiredListener: (ContractUserAuthRequiredViewModel) -> Unit = {
        AppModel.userAuthController(Unit)
    }

    private val paymentAuthRequiredListener: (ContractPaymentAuthRequiredViewModel) -> Unit = {
        AppModel.requestPaymentAuthController.retry()
    }

    private val errorListener: (ContractErrorViewModel) -> Unit = {
        if (isAdded) {
            errorView.setErrorText(it.error)
            errorView.setErrorButtonListener(View.OnClickListener {
                AppModel.selectPaymentOptionController.retry()
            })
            rootContainer.showChild(errorView)
        }
    }

    private val startGooglePayListener: (GooglePayContractViewModel) -> Unit = {
        if (isAdded) {
            rootContainer.showChild(loadingView)
            AppModel.googlePayIntegration?.startGooglePayTokenization(
                fragment = this,
                paymentOptionId = it.paymentOptionId,
                recurringPaymentsPossible = it.recurringPaymentsPossible
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.ym_fragment_contract, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        phoneInput.configureForPhoneInput()
        if (AppModel.userPhoneNumber != null) {
            phoneInput.setText(AppModel.userPhoneNumber)
        }
        phoneInput.setOnEditorActionListener { _, action, _ ->
            (action == EditorInfo.IME_ACTION_DONE).also {
                if (it) {
                    nextButton.performClick()
                }
            }
        }
        phoneInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                phoneInputContainer.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        AppModel.listeners += showContractProgress
        AppModel.listeners += showContract
        AppModel.listeners += restartProcess
        AppModel.listeners += userAuthRequiredListener
        AppModel.listeners += paymentAuthRequiredListener
        AppModel.listeners += errorListener
        AppModel.listeners += startGooglePayListener
    }

    override fun onDestroyView() {
        AppModel.listeners -= showContractProgress
        AppModel.listeners -= showContract
        AppModel.listeners -= restartProcess
        AppModel.listeners -= userAuthRequiredListener
        AppModel.listeners -= paymentAuthRequiredListener
        AppModel.listeners -= errorListener
        AppModel.listeners -= startGooglePayListener

        view?.hideSoftKeyboard()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AppModel.googlePayIntegration?.handleGooglePayTokenization(requestCode, resultCode, data)?.also {
            when (it) {
                is GooglePayTokenizationSuccess -> AppModel.tokenizeController(
                    TokenizeInputModel(
                        paymentOptionId = it.paymentOptionId,
                        allowRecurringPayments = it.recurringPaymentsPossible,
                        paymentOptionInfo = it.paymentOptionInfo
                    )
                )
                is GooglePayTokenizationCanceled -> AppModel.changePaymentOptionController(ChangePaymentOptionInputModel)
                is GooglePayNotHandled -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
