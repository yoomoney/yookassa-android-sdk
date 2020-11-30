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

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.ym_fragment_contract.*
import kotlinx.android.synthetic.main.ym_item_common.*
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.impl.AppModel
import ru.yoo.sdk.kassa.payments.impl.extensions.configureForPhoneInput
import ru.yoo.sdk.kassa.payments.impl.extensions.hideSoftKeyboard
import ru.yoo.sdk.kassa.payments.impl.extensions.isPhoneNumber
import ru.yoo.sdk.kassa.payments.impl.extensions.showChild
import ru.yoo.sdk.kassa.payments.impl.extensions.showSoftKeyboard
import ru.yoo.sdk.kassa.payments.impl.extensions.visible
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.PaymentAuthView
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.GooglePayNotHandled
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.GooglePayTokenizationCanceled
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.GooglePayTokenizationSuccess
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoo.sdk.kassa.payments.payment.changeOption.ChangePaymentOptionInputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.NotRequiredProcessPaymentAuthInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequiredProcessPaymentAuthInputModel
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthInputModel
import ru.yoo.sdk.kassa.payments.utils.SimpleTextWatcher
import ru.yoo.sdk.kassa.payments.utils.showLogoutDialog

internal class ContractFragment : Fragment() {

    private val showContractProgress: (ContractProgressViewModel) -> Unit = {
        if (!isStateSaved) {
            rootContainer.showChild(loadingView)
        }
    }

    private val showContract: (ContractSuccessViewModel) -> Unit = { viewModel ->
        if (!isStateSaved) {
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
                        showLogoutDialog(
                            primaryText.context,
                            name,
                            view
                        )
                    }
                }

                image.setImageDrawable(icon)
                sum.text = amount

                if (fee != null) {
                    feeLayout.visible = true
                    feeView.text = fee
                } else {
                    feeLayout.visible = false
                }

                nextButton.setOnClickListener {
                    view?.hideSoftKeyboard()
                    AppModel.tokenizeController(
                        TokenizeInputModel(
                            paymentOptionId = optionId,
                            savePaymentMethod = shouldSavePaymentMethod(viewModel.savePaymentMethodViewModel)
                        )
                    )
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

            licenseAgreement.apply {
                text = viewModel.licenseAgreement
                movementMethod = LinkMovementMethod.getInstance()
            }

            switchesAndPaymentAuthContainer.visible = viewModel.paymentAuth != null
                    || viewModel.showAllowWalletLinking
                    || viewModel.showPhoneInput
                    || viewModel.savePaymentMethodViewModel != SavePaymentMethodViewModel.UserSelects

            when (viewModel.paymentAuth) {
                null -> {
                    if (viewModel.showPhoneInput) {
                        nextButton.isEnabled = phoneInput.text?.isPhoneNumber ?: false
                        switchesAndPaymentAuthContainer.showChild(additionalInfoInputViewContainer)
                        additionalInfoInputViewContainer.showChild(phoneInput)
                        phoneInput.apply {
                            requestFocus()
                            showSoftKeyboard()
                        }

                        nextButton.setOnClickListener { _ ->
                            view?.hideSoftKeyboard()
                            val text = phoneInput.text
                            if (text != null && text.isPhoneNumber) {
                                AppModel.tokenizeController(
                                    TokenizeInputModel(
                                        paymentOptionId = viewModel.paymentOption.optionId,
                                        paymentOptionInfo = SbolSmsInvoicingInfo(
                                            text.toString()
                                        ),
                                        savePaymentMethod = shouldSavePaymentMethod(viewModel.savePaymentMethodViewModel)
                                    )
                                )
                            } else {
                                phoneInputContainer.error = " "
                            }
                        }
                    } else {
                        nextButton.isEnabled = true
                        switchesAndPaymentAuthContainer.showChild(switchesContainer)

                        allowWalletLinkingContainer.visible = viewModel.showAllowWalletLinking
                        allowWalletLinking.text =
                            allowWalletLinking.context.getString(R.string.ym_allow_wallet_linking)

                        when (viewModel.savePaymentMethodViewModel) {
                            is SavePaymentMethodViewModel.On -> {
                                savePaymentMethodMessage.apply {
                                    text = viewModel.savePaymentMethodViewModel.message
                                    movementMethod = LinkMovementMethod.getInstance()
                                }
                                savePaymentMethodInfoContainer.visible = true
                                savePaymentMethodSelectionContainer.visible = false
                            }
                            is SavePaymentMethodViewModel.Off -> {
                                savePaymentMethodSwitchMessage.apply {
                                    text = viewModel.savePaymentMethodViewModel.switchMessage
                                    movementMethod = LinkMovementMethod.getInstance()
                                }
                                savePaymentMethodInfoContainer.visible = false
                                savePaymentMethodSelectionContainer.visible = true
                            }
                            is SavePaymentMethodViewModel.UserSelects -> {
                                savePaymentMethodInfoContainer.visible = false
                                savePaymentMethodSelectionContainer.visible = false
                            }
                        }
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
                is AuthNotRequiredViewModel -> {
                    AppModel.processPaymentAuthController(
                        NotRequiredProcessPaymentAuthInputModel(
                            allowWalletLinking.isChecked
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
                                RequiredProcessPaymentAuthInputModel(
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
                        view?.hideSoftKeyboard()
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

            if (viewModel.googlePayContractViewModel != null) {
                nextButton.setOnClickListener {
                    view?.hideSoftKeyboard()
                    rootContainer.showChild(loadingView)
                    AppModel.googlePayIntegration?.startGooglePayTokenization(
                        fragment = this,
                        paymentOptionId = viewModel.googlePayContractViewModel.paymentOptionId
                    )
                }
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
        if (!isStateSaved) {
            errorView.setErrorText(it.error)
            errorView.setErrorButtonListener(View.OnClickListener {
                AppModel.tokenizeController.retry()
            })
            rootContainer.showChild(errorView)
        }
    }

    private val startGooglePayListener: (GooglePayContractViewModel) -> Unit = {
        if (!isStateSaved) {
            rootContainer.showChild(loadingView)
            AppModel.googlePayIntegration?.startGooglePayTokenization(
                fragment = this,
                paymentOptionId = it.paymentOptionId
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
        phoneInput.addTextChangedListener(object: SimpleTextWatcher {
            override fun afterTextChanged(s: Editable) {
                phoneInputContainer.error = null
                nextButton.isEnabled = s.toString().isPhoneNumber
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
                        paymentOptionInfo = it.paymentOptionInfo,
                        savePaymentMethod = false
                    )
                )
                is GooglePayTokenizationCanceled -> AppModel.changePaymentOptionController(
                    ChangePaymentOptionInputModel
                )
                is GooglePayNotHandled -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun shouldSavePaymentMethod(savePaymentMethod: SavePaymentMethodViewModel): Boolean {
        return when (savePaymentMethod) {
            is SavePaymentMethodViewModel.On -> true
            is SavePaymentMethodViewModel.UserSelects -> false
            is SavePaymentMethodViewModel.Off -> savePaymentMethodSwitch.isChecked
        }
    }
}
