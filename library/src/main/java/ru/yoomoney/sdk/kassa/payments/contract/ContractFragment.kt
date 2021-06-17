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
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.backButton
import kotlinx.android.synthetic.main.ym_fragment_contract.additionalInfoInputViewContainer
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
import kotlinx.android.synthetic.main.ym_fragment_contract.rootContainer
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodMessageSubTitle
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodMessageTitle
import kotlinx.android.synthetic.main.ym_fragment_contract.savePaymentMethodSelection
import kotlinx.android.synthetic.main.ym_fragment_contract.sberPayView
import kotlinx.android.synthetic.main.ym_fragment_contract.subtitle
import kotlinx.android.synthetic.main.ym_fragment_contract.sum
import kotlinx.android.synthetic.main.ym_fragment_contract.switches
import kotlinx.android.synthetic.main.ym_fragment_contract.switchesContainer
import kotlinx.android.synthetic.main.ym_fragment_contract.title
import kotlinx.android.synthetic.main.ym_fragment_contract.topBar
import kotlinx.android.synthetic.main.ym_fragment_contract.yooMoneyAccountView
import kotlinx.android.synthetic.main.ym_item_common.image
import kotlinx.android.synthetic.main.ym_item_common.primaryText
import kotlinx.android.synthetic.main.ym_item_common.secondaryText
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooAction
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooImage
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooSubtitle
import kotlinx.android.synthetic.main.ym_yoo_money_info_view.yooTitle
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodId
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Effect
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State
import ru.yoomoney.sdk.kassa.payments.contract.ContractFormatter.Companion.getSwitchOnSavePaymentMethodTitle
import ru.yoomoney.sdk.kassa.payments.contract.ContractFormatter.Companion.getUserSelectsSavePaymentMethodTitle
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule
import ru.yoomoney.sdk.kassa.payments.extensions.configureForPhoneInput
import ru.yoomoney.sdk.kassa.payments.extensions.format
import ru.yoomoney.sdk.kassa.payments.extensions.getAdditionalInfo
import ru.yoomoney.sdk.kassa.payments.extensions.getIcon
import ru.yoomoney.sdk.kassa.payments.extensions.getTitle
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
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoomoney.sdk.kassa.payments.model.NewCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.payment.googlePay.GooglePayRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentAuthRequiredOutputModel
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardAnalyticsLogger
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardEvent
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
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.SberPay
import java.math.BigDecimal
import javax.inject.Inject

internal typealias ContractViewModel = RuntimeViewModel<State, Action, Effect>

private const val REQUEST_CODE_SCAN_BANK_CARD = 0x37BD

internal class ContractFragment : Fragment(R.layout.ym_fragment_contract) {

    @Inject
    lateinit var paymentParameters: PaymentParameters

    @Inject
    lateinit var loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository

    @Inject
    @JvmField
    var paymentMethodId: PaymentMethodId? = null

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var userAuthInfoRepository: TokensStorage

    @Inject
    lateinit var googlePayRepository: GooglePayRepository

    @Inject
    lateinit var reporter: Reporter

    private val viewModel: ContractViewModel by viewModel(ContractModule.CONTRACT) { viewModelFactory }

    private val topBarAnimator: ViewPropertyAnimator? by lazy { topBar?.animate() }

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

    private fun setupView() {
        if (isTablet) {
            rootContainer.updateLayoutParams<ViewGroup.LayoutParams> { height = resources.getDimensionPixelSize(R.dimen.ym_dialogHeight) }
        }
        topBar.takeIf { loadedPaymentOptionListRepository.getLoadedPaymentOptions().size == 1 }
            ?.backButton?.hide()
            ?: topBar.onBackButton {
                parentFragmentManager.popBackStack()
                contentView.hideSoftKeyboard()
                router.navigateTo(Screen.PaymentOptions())
            }

        phoneInput.editText.configureForPhoneInput()
        val userPhoneNumber = paymentParameters.userPhoneNumber
        if (userPhoneNumber != null) {
            phoneInput.text = userPhoneNumber
        }
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
        bankCardView.setBankCardAnalyticsLogger(object: BankCardAnalyticsLogger {
            override fun onNewEvent(event: BankCardEvent) {
                reporter.report("actionBankCardForm", event.toString())
            }
        })
    }

    fun reload() {
        showLoadingState()
        viewModel.handleAction(Action.Load)
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

    fun paymentAuthResult(result: Screen.PaymentAuth.PaymentAuthResult) {
        when (result) {
            Screen.PaymentAuth.PaymentAuthResult.SUCCESS -> viewModel.handleAction(Action.PaymentAuthSuccess)
            Screen.PaymentAuth.PaymentAuthResult.CANCEL -> viewModel.handleAction(Action.PaymentAuthCancel)
        }
    }

    private fun showState(state: State) {
        showState(!isTablet) {
            when (state) {
                State.Loading -> showLoadingState()
                is State.Content -> showContentState(state)
                is State.GooglePay -> showGooglePayState(state.paymentOptionId)
                is State.Error -> showErrorState(state.error) {
                    viewModel.handleAction(Action.Load)
                }
                is State.TokenizeError -> showErrorState(state.error) {
                    viewModel.handleAction(Action.Tokenize(state.paymentOptionInfo))
                }
                is State.Tokenize -> {
                    showLoadingState()
                    resumePostponedTransition(rootContainer)
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
        val savePaymentMethod = paymentParameters.savePaymentMethod.takeIf {
            content.paymentOption.savePaymentMethodAllowed
        } ?: SavePaymentMethod.OFF

        rootContainer.showChild(contentView)
        title.text = content.shopTitle
        subtitle.text = content.shopSubtitle

        showPaymentOptionInfo(content, savePaymentMethod)
        resumePostponedTransition(rootContainer)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.getViewHeight() }
    }

    private fun showPaymentOptionInfo(content: State.Content, savePaymentMethod: SavePaymentMethod) {
        with(content.paymentOption) {
            val name = content.paymentOption.getTitle(requireContext())
            primaryText.text = name
            topBar.title = name

            secondaryText.text = content.paymentOption.getAdditionalInfo()
            secondaryText.visible = content.paymentOption.getAdditionalInfo() != null
            // suddenly, secondaryText not shown at first time, so we need this hack
            secondaryText.parent.requestLayout()


            val serviceFee = fee?.service
            val feeString = if (serviceFee != null && (serviceFee.value > BigDecimal.ZERO)) {
                serviceFee.format()
            } else {
                null
            }

            image.setImageDrawable(getIcon(requireContext()))
            sum.text = charge.format().makeStartBold()

            if (feeString != null) {
                feeLayout.visible = true
                feeView.text = feeString
            } else {
                feeLayout.visible = false
            }

            nextButton.setOnClickListener {
                view?.hideSoftKeyboard()
                viewModel.handleAction(Action.Tokenize())
            }


            setUpPaymentAuth(content, savePaymentMethod)

            when (this) {
                is Wallet -> setUpAuthorizedWallet(content, this)
                is GooglePay -> setUpGooglePlay(content.paymentOption.id)
                is NewCard -> setUpBankCardView(content.paymentOption.id, savePaymentMethod)
                is LinkedCard -> setUpLinkedBankCardView(content, pan.replace("*", "•"))
                is PaymentIdCscConfirmation -> setUpLinkedBankCardView(content, ("$first••••••$last"))
                is AbstractWallet -> {
                    setUpAbstractWallet()
                    return
                }
                is SberBank -> setUpSberbankView(content, this)
            }


            licenseAgreement.apply {
                text = getLicenseAgreementText()
                movementMethod = LinkMovementMethod.getInstance()
            }

            switches.visible = content.showAllowWalletLinking
                    || content.confirmation !is MobileApplication
                    || (savePaymentMethod != SavePaymentMethod.OFF && content.paymentOption !is SberBank)
        }
    }

    private fun setUpPaymentAuth(
        content: State.Content,
        savePaymentMethod: SavePaymentMethod
    ) {
        if (content.paymentOption !is SberBank) {
            nextButton.isEnabled = isNextButtonEnabled(content.paymentOption)
            switches.showChild(switchesContainer)

            allowWalletLinking.visible = content.showAllowWalletLinking
            allowWalletLinking.title = getString(R.string.ym_contract_link_wallet_title)
            allowWalletLinking.description = allowWalletLinking.context.getString(R.string.ym_allow_wallet_linking)

            when (savePaymentMethod) {
                SavePaymentMethod.ON -> {
                    savePaymentMethodMessageTitle.text = getSwitchOnSavePaymentMethodTitle(requireContext(), content.paymentOption)
                    savePaymentMethodMessageSubTitle.apply {
                        text = ContractFormatter.getSavePaymentMethodMessageLink(
                            requireContext(),
                            content.paymentOption
                        )
                        movementMethod = LinkMovementMethod.getInstance()
                    }

                    savePaymentMethodMessageTitle.visible = true
                    savePaymentMethodMessageSubTitle.visible = true
                    savePaymentMethodSelection.visible = false
                }
                SavePaymentMethod.USER_SELECTS -> {
                    savePaymentMethodSelection.apply {
                        title = getUserSelectsSavePaymentMethodTitle(requireContext(), content.paymentOption)
                        with(findViewById<TextView>(R.id.descriptionView)) {
                            movementMethod = LinkMovementMethod.getInstance()
                            text = ContractFormatter.getSavePaymentMethodSwitchLink(
                                requireContext(),
                                content.paymentOption
                            )
                        }
                        onCheckedChangedListener { savePaymentMethod ->
                            viewModel.handleAction(Action.ChangeSavePaymentMethod(savePaymentMethod))
                        }
                    }
                    savePaymentMethodMessageTitle.visible = false
                    savePaymentMethodMessageSubTitle.visible = true
                    savePaymentMethodSelection.visible = true
                }
                SavePaymentMethod.OFF -> {
                    savePaymentMethodMessageTitle.visible = false
                    savePaymentMethodMessageSubTitle.visible = false
                    savePaymentMethodSelection.visible = false
                }
            }
        }
    }

    private fun showGooglePayState(paymentOptionId: Int) {
        showLoadingState()
        rootContainer.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.height + 1 }
        resumePostponedTransition(rootContainer)

        googlePayRepository.startGooglePayTokenize(
            fragment = this,
            paymentOptionId = paymentOptionId
        )
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
            is Effect.TokenizeComplete -> {
                when (val model = effect.tokenizeOutputModel) {
                    is TokenOutputModel -> router.navigateTo(Screen.TokenizeSuccessful(model))
                    is TokenizePaymentAuthRequiredOutputModel -> router.navigateTo(
                        Screen.PaymentAuth(model.charge, allowWalletLinking.isChecked)
                    )
                }
            }
            is Effect.PaymentAuthRequired -> router.navigateTo(
                Screen.PaymentAuth(effect.charge, allowWalletLinking.isChecked)
            )
            Effect.RestartProcess -> {
                parentFragmentManager.popBackStack()
                router.navigateTo(Screen.PaymentOptions())
            }
            Effect.CancelProcess -> router.navigateTo(Screen.TokenizeCancelled)
        }
    }

    private fun setUpAuthorizedWallet(content: State.Content, wallet: Wallet) {
        yooMoneyAccountView.show()
        yooSubtitle.show()
        yooAction.show()

        yooTitle.text = userAuthInfoRepository.userAuthName ?: wallet.walletId
        yooSubtitle.text = wallet.balance.format()

        allowWalletLinking.isChecked = content.allowWalletLinking

        userAuthInfoRepository.userAvatarUrl?.let {
            Picasso.get().load(Uri.parse(it))
                .placeholder(R.drawable.ym_user_avatar)
                .cropToCircle()
                .into(yooImage)
        } ?: yooImage.setImageResource(R.drawable.ym_user_avatar)

        yooAction.setOnClickListener {
            val content = YmAlertDialog.DialogContent(
                title = getString(
                    R.string.ym_logout_dialog_message,
                    userAuthInfoRepository.userAuthName ?: wallet.walletId
                ),
                content = null,
                actionPositiveText = getString(R.string.ym_logout_dialog_button_positive),
                actionNegativeText = getString(R.string.ym_logout_dialog_button_negative),
                isPositiveActionAlert = true
            )

            CheckoutAlertDialog.create(childFragmentManager, content = content, dimAmount = 0.6f).apply {
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

    private fun setUpBankCardView(id: Int, savePaymentMethod: SavePaymentMethod) {
        with(bankCardView) {
            show()

            setOnBankCardReadyListener { cardInfo ->
                nextButton.isEnabled = true
                nextButton.setOnClickListener {
                    viewModel.handleAction(Action.Tokenize(cardInfo))
                }
                view?.hideSoftKeyboard()
            }

            setOnBankCardNotReadyListener { nextButton.isEnabled = false }

            setOnBankCardScanListener { startActivityForResult(it,
                REQUEST_CODE_SCAN_BANK_CARD
            ) }
        }
    }

    private fun setUpLinkedBankCardView(
        content: State.Content,
        cardNumber: String
    ) {
        allowWalletLinking.isChecked = content.allowWalletLinking

        with(bankCardView) {
            show()
            presetBankCardInfo(cardNumber)

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

    private fun setUpSberbankView(content: State.Content, sber: SberBank) {
        if (content.confirmation is MobileApplication) {
            sberPayView.show()
            nextButton.isEnabled = true
            nextButton.setOnClickListener {
                viewModel.handleAction(Action.Tokenize(SberPay))
            }
        } else  {
            nextButton.isEnabled = phoneInput.text?.isPhoneNumber ?: false
            switches.showChild(additionalInfoInputViewContainer)
            additionalInfoInputViewContainer.showChild(phoneInput)


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
            is NewCard, is LinkedCard, is PaymentIdCscConfirmation -> false
            else -> true
        }
    }

    private fun getLicenseAgreementText(): CharSequence {
        return getMessageWithLink(
            requireContext(),
            R.string.ym_license_agreement_part_1,
            R.string.ym_license_agreement_part_2
        ) {
            ContextCompat.startActivity(
                requireContext(),
                WebViewActivity.create(
                    requireContext(),
                    requireContext().getString(R.string.ym_license_agreement_url)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
            )
        }
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