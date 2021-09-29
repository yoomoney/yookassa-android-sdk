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

package ru.yoomoney.sdk.kassa.payments.contract

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.backButton
import kotlinx.android.synthetic.main.ym_fragment_contract.allowWalletLinking
import kotlinx.android.synthetic.main.ym_fragment_contract.bankCardView
import kotlinx.android.synthetic.main.ym_fragment_contract.contentView
import kotlinx.android.synthetic.main.ym_fragment_contract.contractScrollView
import kotlinx.android.synthetic.main.ym_fragment_contract.errorView
import kotlinx.android.synthetic.main.ym_fragment_contract.feeLayout
import kotlinx.android.synthetic.main.ym_fragment_contract.feeView
import kotlinx.android.synthetic.main.ym_fragment_contract.googlePayView
import kotlinx.android.synthetic.main.ym_fragment_contract.licenseAgreement
import kotlinx.android.synthetic.main.ym_fragment_contract.loadingView
import kotlinx.android.synthetic.main.ym_fragment_contract.nextButton
import kotlinx.android.synthetic.main.ym_fragment_contract.phoneInput
import kotlinx.android.synthetic.main.ym_fragment_contract.phoneInputContainer
import kotlinx.android.synthetic.main.ym_fragment_contract.rootContainer
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodMessageSubTitle
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodMessageTitle
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodSelection
import kotlinx.android.synthetic.main.ym_fragment_contract.sberPayView
import kotlinx.android.synthetic.main.ym_fragment_contract.subtitle
import kotlinx.android.synthetic.main.ym_fragment_contract.sum
import kotlinx.android.synthetic.main.ym_fragment_contract.switches
import kotlinx.android.synthetic.main.ym_fragment_contract.title
import kotlinx.android.synthetic.main.ym_fragment_contract.topBar
import kotlinx.android.synthetic.main.ym_fragment_contract.yooMoneyAccountView
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooAction
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooImage
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooSubtitle
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooTitle
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Effect
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule
import ru.yoomoney.sdk.kassa.payments.extensions.configureForPhoneInput
import ru.yoomoney.sdk.kassa.payments.extensions.format
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.isPhoneNumber
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.extensions.visible
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayNotHandled
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayTokenizationCanceled
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayTokenizationSuccess
import ru.yoomoney.sdk.kassa.payments.ui.view.cropToCircle
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderTitle
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardAnalyticsLogger
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardEvent
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.onCheckedChangedListener
import ru.yoomoney.sdk.kassa.payments.ui.resumePostponedTransition
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutAlertDialog
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_CARD_NUMBER
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_MONTH
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_YEAR
import ru.yoomoney.sdk.kassa.payments.utils.SimpleTextWatcher
import ru.yoomoney.sdk.kassa.payments.utils.WebViewActivity
import ru.yoomoney.sdk.kassa.payments.utils.getMessageWithLink
import ru.yoomoney.sdk.kassa.payments.utils.show
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.SberPay
import ru.yoomoney.sdk.kassa.payments.model.formatService
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.utils.formatHtmlWithLinks
import ru.yoomoney.sdk.kassa.payments.utils.getBankOrBrandLogo
import javax.inject.Inject

internal typealias ContractViewModel = RuntimeViewModel<State, Action, Effect>

private const val REQUEST_CODE_SCAN_BANK_CARD = 0x37BD

internal class ContractFragment : Fragment(R.layout.ym_fragment_contract) {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var googlePayRepository: GooglePayRepository

    @Inject
    lateinit var reporter: Reporter

    private val viewModel: ContractViewModel by viewModel(ContractModule.CONTRACT) { viewModelFactory }

    private val topBarAnimator: ViewPropertyAnimator? by lazy { topBar?.animate() }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectContractFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isTablet) {
            postponeEnterTransition()
        }
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setFragmentResultListener(TokenizeFragment.TOKENIZE_RESULT_KEY) { key, bundle ->
            val result =
                bundle.getSerializable(TokenizeFragment.TOKENIZE_RESULT_EXTRA) as Screen.Tokenize.TokenizeResult
            if (result == Screen.Tokenize.TokenizeResult.CANCEL) {
                viewModel.handleAction(Action.TokenizeCancelled)
            }
        }

        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {
                showError(it) {
                    viewModel.handleAction(Action.Load)
                }
            }
        )
    }

    private fun onBackPressed() {
        contentView.hideSoftKeyboard()
        parentFragmentManager.popBackStack()
        router.navigateTo(Screen.PaymentOptions())
    }

    private fun setupView() {
        if (isTablet) {
            rootContainer.updateLayoutParams<ViewGroup.LayoutParams> {
                height = resources.getDimensionPixelSize(R.dimen.ym_dialogHeight)
            }
        }

        phoneInput.editText.configureForPhoneInput()
        phoneInput.editText.setOnEditorActionListener { _, action, _ ->
            (action == EditorInfo.IME_ACTION_DONE).also {
                if (it) {
                    nextButton.performClick()
                }
            }
        }
        phoneInput.editText.addTextChangedListener(object : SimpleTextWatcher {
            override fun afterTextChanged(s: Editable) {
                phoneInput.error = ""
                phoneInput.hint = ""
                nextButton.isEnabled = s.toString().isPhoneNumber
            }
        })
        allowWalletLinking.onCheckedChangedListener {
            viewModel.handleAction(Action.ChangeAllowWalletLinking(it))
        }
        initOnScrollChangeListener()
        bankCardView.setBankCardAnalyticsLogger(object : BankCardAnalyticsLogger {
            override fun onNewEvent(event: BankCardEvent) {
                reporter.report("actionBankCardForm", event.toString())
            }
        })
    }

    override fun onDestroyView() {
        view?.hideSoftKeyboard()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCAN_BANK_CARD && resultCode == Activity.RESULT_OK) {
            data?.extras?.apply {
                val cardNumber = getString(EXTRA_CARD_NUMBER)?.filter(Char::isDigit)
                val expiryMonth = getInt(EXTRA_EXPIRY_MONTH).takeIf { it > 0 }
                val expiryYear = getInt(EXTRA_EXPIRY_YEAR).takeIf { it > 0 }?.rem(100)

                bankCardView.setBankCardInfo(cardNumber, expiryYear, expiryMonth)
            }
        } else {
            googlePayRepository.handleGooglePayTokenize(requestCode, resultCode, data).also {
                when (it) {
                    is GooglePayTokenizationSuccess -> viewModel.handleAction(Action.Tokenize(it.paymentOptionInfo))
                    is GooglePayTokenizationCanceled -> viewModel.handleAction(Action.GooglePayCancelled)
                    is GooglePayNotHandled -> super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun showState(state: State) {
        showState(!isTablet) {
            when (state) {
                State.Loading -> showLoadingState()
                is State.Content -> showContentState(state)
                is State.GooglePay -> showGooglePayState()
                is State.Error -> showErrorState(state.error) {
                    viewModel.handleAction(Action.Load)
                }
            }
        }
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(rootContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showLoadingState() {
        rootContainer.showChild(loadingView)
    }

    private fun showContentState(content: State.Content) {
        rootContainer.showChild(contentView)
        if (content.isSinglePaymentMethod) {
            onBackPressedCallback.remove()
            topBar.backButton?.hide()
        } else {
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
            topBar.onBackButton { onBackPressed() }
        }
        title.text = content.shopTitle
        subtitle.text = content.shopSubtitle

        val paymentOption = content.contractInfo.paymentOption
        topBar.title = paymentOption.title ?: paymentOption.getPlaceholderTitle(requireContext())
        sum.text = paymentOption.charge.format().makeStartBold()
        setUpFee(paymentOption.fee)
        nextButton.setOnClickListener {
            view?.hideSoftKeyboard()
            viewModel.handleAction(Action.Tokenize())
        }

        val savePaymentMethodOption = content.getSavePaymentMethodOption()
        setUpSavePaymentMethodOption(content, savePaymentMethodOption)

        nextButton.isEnabled = isNextButtonEnabled(content.contractInfo.paymentOption)
        switches.visible = content.confirmation !is MobileApplication || (content.savePaymentMethod != SavePaymentMethod.OFF)
        showContractInfo(content, content.contractInfo)
        resumePostponedTransition(rootContainer)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.getViewHeight() }
    }

    private fun showContractInfo(content: State.Content, contractInfo: ContractInfo) {
        when (contractInfo) {
            is ContractInfo.WalletContractInfo -> {
                setUpWalletLinking(contractInfo.showAllowWalletLinking)
                setUpAuthorizedWallet(contractInfo, contractInfo.paymentOption)
                switches.visible = contractInfo.showAllowWalletLinking
            }
            is ContractInfo.WalletLinkedCardContractInfo -> {
                setUpWalletLinking(contractInfo.showAllowWalletLinking)
                allowWalletLinking.isChecked = contractInfo.allowWalletLinking
                setUpLinkedBankCardView(
                    contractInfo.paymentOption.pan.replace("*", "•"),
                    contractInfo.paymentOption.brand
                )
                switches.visible = contractInfo.showAllowWalletLinking
            }
            is ContractInfo.PaymentIdCscConfirmationContractInfo -> {
                val first = contractInfo.paymentOption.first
                val last = contractInfo.paymentOption.last
                val brand = contractInfo.paymentOption.brand
                allowWalletLinking.isChecked = contractInfo.allowWalletLinking
                setUpLinkedBankCardView(("$first••••••$last"), brand)
            }
            is ContractInfo.LinkedBankCardContractInfo -> setUpBankCardView(contractInfo.instrument)
            is ContractInfo.NewBankCardContractInfo -> setUpBankCardView(null)
            is ContractInfo.GooglePayContractInfo -> setUpGooglePlay(content.contractInfo.paymentOption.id)
            is ContractInfo.SberBankContractInfo -> setUpSberbankView(content, contractInfo.userPhoneNumber)
            is ContractInfo.AbstractWalletContractInfo -> setUpAbstractWallet()
        }

        licenseAgreement.apply {
            text = getTransactionAgreementText(content)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setUpCardFormWithCsc(instrumentBankCard: PaymentInstrumentBankCard) {
        with(bankCardView) {
            presetBankCardInfo(instrumentBankCard.cardNumber)
            showBankLogo(getBankOrBrandLogo(instrumentBankCard.cardNumber, instrumentBankCard.cardType))
            setOnPresetBankCardReadyListener { cvc ->
                nextButton.isEnabled = true
                nextButton.setOnClickListener {
                    viewModel.handleAction(Action.TokenizePaymentInstrument(instrumentBankCard, cvc))
                }
                view?.hideSoftKeyboard()
            }
        }
    }

    private fun setUpCardFormWithoutCsc(instrumentBankCard: PaymentInstrumentBankCard) {
        with(bankCardView) {
            setCardData(instrumentBankCard.cardNumber)
            setChangeCardAvailable(false)
            hideAdditionalInfo()
            showBankLogo(getBankOrBrandLogo(instrumentBankCard.cardNumber, instrumentBankCard.cardType))
        }
        nextButton.isEnabled = true
        nextButton.setOnClickListener {
            viewModel.handleAction(Action.TokenizePaymentInstrument(instrumentBankCard, null))
        }
    }

    private fun setUpFee(fee: Fee?) {
        val feeString = fee.formatService()
        if (feeString != null) {
            feeLayout.visible = true
            feeView.text = feeString
        } else {
            feeLayout.visible = false
        }
    }

    private fun setUpWalletLinking(showAllowWalletLinking: Boolean) {
        allowWalletLinking.visible = showAllowWalletLinking
        allowWalletLinking.title = getString(R.string.ym_contract_link_wallet_title)
        allowWalletLinking.description = allowWalletLinking.context.getString(R.string.ym_allow_wallet_linking)
    }

    private fun setUpSavePaymentMethodOption(content: State.Content, option: SavePaymentMethodOption) {
        when (option) {
            is SavePaymentMethodOption.SwitchSavePaymentMethodOption -> setUpSwitchSavePaymentMethodOption(
                option,
                content.shouldSavePaymentMethod || content.shouldSavePaymentInstrument
            )
            is SavePaymentMethodOption.MessageSavePaymentMethodOption -> setUpMessageSavePaymentMethodOption(option)
            is SavePaymentMethodOption.None -> {
                savePaymentMethodMessageTitle.visible = false
                savePaymentMethodMessageSubTitle.visible = false
                savePaymentMethodSelection.visible = false
            }
        }
    }

    private fun setUpSwitchSavePaymentMethodOption(savePaymentMethodOption: SavePaymentMethodOption.SwitchSavePaymentMethodOption, checked: Boolean) {
        savePaymentMethodSelection.apply {
            title = savePaymentMethodOption.title
            with(findViewById<TextView>(R.id.descriptionView)) {
                movementMethod = LinkMovementMethod.getInstance()
                text = formatHtmlWithLinks(savePaymentMethodOption.subtitle) {
                    ContextCompat.startActivity(
                        requireContext(),
                        SavePaymentMethodInfoActivity.create(
                            requireContext(),
                            savePaymentMethodOption.screenTitle,
                            savePaymentMethodOption.screenText
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
                    )
                }
            }
            onCheckedChangedListener(null)
            isChecked = checked
            onCheckedChangedListener { savePaymentMethod ->
                viewModel.handleAction(Action.ChangeSavePaymentMethod(savePaymentMethod))
            }
        }
        savePaymentMethodMessageTitle.visible = false
        savePaymentMethodMessageSubTitle.visible = false
        savePaymentMethodSelection.visible = true
    }

    private fun setUpMessageSavePaymentMethodOption(option: SavePaymentMethodOption.MessageSavePaymentMethodOption) {
        savePaymentMethodMessageTitle.text = option.title
        savePaymentMethodMessageSubTitle.apply {
            text = formatHtmlWithLinks(option.subtitle) {
                ContextCompat.startActivity(
                    requireContext(),
                    SavePaymentMethodInfoActivity.create(
                        requireContext(),
                        option.screenTitle,
                        option.screenText
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
                )
            }
            movementMethod = LinkMovementMethod.getInstance()
        }

        savePaymentMethodMessageTitle.visible = true
        savePaymentMethodMessageSubTitle.visible = true
        savePaymentMethodSelection.visible = false
    }

    private fun showGooglePayState() {
        showLoadingState()
        rootContainer.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.height + 1 }
        resumePostponedTransition(rootContainer)
    }

    private fun showErrorState(error: Throwable, action: () -> Unit) {
        when (error) {
            is ApiMethodException -> when (error.error.errorCode) {
                ErrorCode.INVALID_TOKEN -> router.navigateTo(Screen.PaymentOptions())
                ErrorCode.INVALID_CONTEXT, ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED, ErrorCode.SESSIONS_EXCEEDED -> router.navigateTo(
                    Screen.PaymentOptions()
                )
                else -> showError(error, action)
            }
            else -> showError(error, action)
        }
        resumePostponedTransition(rootContainer)
    }

    private fun showError(throwable: Throwable, action: () -> Unit) {
        errorView.setErrorText(errorFormatter.format(throwable))
        errorView.setErrorButtonListener(View.OnClickListener {
            action()
        })
        rootContainer.showChild(errorView)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.getViewHeight() }
    }

    private fun showEffect(effect: Effect) {
        when (effect) {
            is Effect.StartGooglePay -> googlePayRepository.startGooglePayTokenize(
                fragment = this,
                paymentOptionId = effect.paymentOptionId
            )
            is Effect.ShowTokenize -> router.navigateTo(Screen.Tokenize(effect.tokenizeInputModel))
            Effect.RestartProcess -> {
                parentFragmentManager.popBackStack()
                router.navigateTo(Screen.PaymentOptions())
            }
            Effect.CancelProcess -> router.navigateTo(Screen.TokenizeCancelled)
        }
    }

    private fun setUpAuthorizedWallet(contractInfo: ContractInfo.WalletContractInfo, wallet: Wallet) {
        yooMoneyAccountView.show()
        yooSubtitle.show()
        yooAction.show()

        yooTitle.text = contractInfo.walletUserAuthName ?: wallet.walletId
        yooSubtitle.text = wallet.balance.format()

        allowWalletLinking.isChecked = contractInfo.allowWalletLinking

        contractInfo.walletUserAvatarUrl?.let {
            Picasso.get().load(Uri.parse(it))
                .placeholder(R.drawable.ym_user_avatar)
                .cropToCircle()
                .into(yooImage)
        } ?: yooImage.setImageResource(R.drawable.ym_user_avatar)

        yooAction.setOnClickListener {
            val dialogContent = YmAlertDialog.DialogContent(
                title = getString(
                    R.string.ym_logout_dialog_message,
                    contractInfo.walletUserAuthName ?: wallet.walletId
                ),
                content = null,
                actionPositiveText = getString(R.string.ym_logout_dialog_button_positive),
                actionNegativeText = getString(R.string.ym_logout_dialog_button_negative),
                isPositiveActionAlert = true
            )

            CheckoutAlertDialog.create(childFragmentManager, content = dialogContent, dimAmount = 0.6f).apply {
                attachListener(object : YmAlertDialog.DialogListener {
                    override fun onPositiveClick() {
                        viewModel.handleAction(Action.Logout)
                    }
                })
            }.show(childFragmentManager)
        }
    }

    private fun setUpAbstractWallet() {
        router.navigateTo(Screen.MoneyAuth)
    }

    private fun setUpGooglePlay(paymentOptionId: Int) {
        googlePayView.show()
        nextButton.setOnClickListener {
            view?.hideSoftKeyboard()
            rootContainer.showChild(loadingView)
            googlePayRepository.startGooglePayTokenize(
                fragment = this,
                paymentOptionId = paymentOptionId
            )
        }
    }

    private fun setUpBankCardView(instrumentBankCard: PaymentInstrumentBankCard?) {
        with(bankCardView) {
            show()
            if (instrumentBankCard != null) {
                if (instrumentBankCard.cscRequired) {
                    setUpCardFormWithCsc(instrumentBankCard)
                } else {
                    setUpCardFormWithoutCsc(instrumentBankCard)
                }
            } else {
                setOnBankCardReadyListener { cardInfo ->
                    nextButton.isEnabled = true
                    nextButton.setOnClickListener {
                        viewModel.handleAction(Action.Tokenize(cardInfo))
                    }
                    view?.hideSoftKeyboard()
                }
                setOnBankCardScanListener {
                    startActivityForResult(it, REQUEST_CODE_SCAN_BANK_CARD)
                }
            }
            setOnBankCardNotReadyListener { nextButton.isEnabled = false }

            setOnBankCardScanListener {
                startActivityForResult(
                    it,
                    REQUEST_CODE_SCAN_BANK_CARD
                )
            }
        }
    }

    private fun setUpLinkedBankCardView(cardNumber: String, brand: CardBrand) {
        with(bankCardView) {
            show()
            presetBankCardInfo(cardNumber)
            bankCardView.showBankLogo(getBankOrBrandLogo(cardNumber, brand))

            setOnPresetBankCardReadyListener { cvc ->
                nextButton.isEnabled = true
                nextButton.setOnClickListener {
                    viewModel.handleAction(Action.Tokenize(LinkedCardInfo(cvc)))
                }
                view?.hideSoftKeyboard()
            }

            setOnBankCardNotReadyListener { nextButton.isEnabled = false }
        }
    }

    private fun setUpSberbankView(content: State.Content, userPhoneNumber: String?) {
        if (userPhoneNumber != null) {
            phoneInput.text = userPhoneNumber
        }
        if (content.confirmation is MobileApplication) {
            sberPayView.show()
            nextButton.isEnabled = true
            nextButton.setOnClickListener {
                viewModel.handleAction(Action.Tokenize(SberPay))
            }
        } else {
            nextButton.isEnabled = phoneInput.text?.isPhoneNumber ?: false
            phoneInputContainer.show()

            nextButton.setOnClickListener { _ ->
                view?.hideSoftKeyboard()
                val text = phoneInput.text
                if (text != null && text.isPhoneNumber) {
                    viewModel.handleAction(Action.Tokenize(SbolSmsInvoicingInfo(text.toString())))
                } else {
                    phoneInput.error = " "
                }
            }
        }
    }

    private fun isNextButtonEnabled(paymentOption: PaymentOption): Boolean {
        return when (paymentOption) {
            is BankCardPaymentOption, is LinkedCard, is PaymentIdCscConfirmation -> false
            else -> true
        }
    }

    private fun getLicenseAgreementText(content: State.Content): CharSequence {
        return formatHtmlWithLinks(content.userAgreementUrl) {
            ContextCompat.startActivity(
                requireContext(),
                WebViewActivity.create(
                    requireContext(),
                    it
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
            )
        }
    }

    private fun getTransactionAgreementText(content: State.Content): CharSequence {
        val result = SpannableStringBuilder()
        result.append(getLicenseAgreementText(content))
        if (content.isSplitPayment) {
            result.append("\n")
            result.append(getSafeTransactionAgreementText())
        }
        return result
    }

    private fun getSafeTransactionAgreementText() = getMessageWithLink(
        requireContext(),
        R.string.ym_safe_payments_agreement_part_1,
        R.string.ym_safe_payments_agreement_part_2
    ) {
        ContextCompat.startActivity(
            requireContext(),
            SavePaymentMethodInfoActivity.create(
                requireContext(),
                R.string.ym_safe_payments_agreement_title,
                R.string.ym_safe_payments_agreement_message,
                null
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        topBarAnimator?.cancel()
    }

    private fun initOnScrollChangeListener() {
        val elevation = requireContext().resources.getDimension(ru.yoomoney.sdk.gui.gui.R.dimen.ym_elevationXS)
        contractScrollView.viewTreeObserver.addOnScrollChangedListener {
            topBarAnimator
                ?.takeIf { contractScrollView?.scrollY != null }
                ?.translationZ(if (contractScrollView.scrollY > 0) elevation else .0f)
                ?.start()
        }
    }
}

private fun CharSequence.makeStartBold() =
    (this as? Spannable ?: SpannableStringBuilder(this)).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }