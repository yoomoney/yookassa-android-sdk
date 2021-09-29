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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import android.content.DialogInterface
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.logo
import kotlinx.android.synthetic.main.ym_fragment_payment_options.contentContainer
import kotlinx.android.synthetic.main.ym_fragment_payment_options.topBar
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsListFormatter
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsModule.Companion.PAYMENT_OPTIONS
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.getAdditionalInfo
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderIcon
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderTitle
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.extensions.showSnackbar
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutAlertDialog
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeConfig
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeItemHelper
import ru.yoomoney.sdk.kassa.payments.ui.view.ErrorView
import ru.yoomoney.sdk.kassa.payments.ui.view.LoadingView
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.utils.getBankOrBrandLogo
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias PaymentOptionListViewModel = RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>

internal class PaymentOptionListFragment : Fragment(R.layout.ym_fragment_payment_options),
    PaymentOptionListRecyclerViewAdapter.PaymentOptionClickListener {

    @Inject
    lateinit var uiParameters: UiParameters

    @Inject
    @PaymentOptionsListFormatter
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: LoadingView
    private lateinit var errorView: ErrorView

    private val swipeItemHelper: SwipeItemHelper by lazy {
        val resources = requireContext().resources
        val swipeConfig = SwipeConfig.get(
            resources.getInteger(android.R.integer.config_shortAnimTime),
            resources.getDimensionPixelSize(R.dimen.ym_space5XL),
            MENU_ITEM_COUNT
        )
        SwipeItemHelper(requireContext(), swipeConfig)
    }

    private val viewModel: PaymentOptionListViewModel by viewModel(PAYMENT_OPTIONS) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectPaymentOptionListFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resources = view.resources
        val isTablet = resources.getBoolean(R.bool.ym_isTablet)
        val minHeight = resources.getDimensionPixelSize(R.dimen.ym_viewAnimator_maxHeight).takeIf { !isTablet }
        val minLoadingHeight =
            resources.getDimensionPixelSize(R.dimen.ym_payment_options_loading_min_height).takeIf { !isTablet }

            setFragmentResultListener(MoneyAuthFragment.MONEY_AUTH_RESULT_KEY) { _, bundle ->
            val result = bundle.getSerializable(MoneyAuthFragment.MONEY_AUTH_RESULT_EXTRA) as Screen.MoneyAuth.Result
            onAuthResult(result)
        }

        setFragmentResultListener(UnbindCardFragment.UNBIND_CARD_RESULT_KEY) { _, bundle ->
            val result =
                requireNotNull(bundle.getParcelable<Screen.UnbindInstrument.Success>(UnbindCardFragment.UNBIND_CARD_RESULT_EXTRA))
            onUnbindingCardResult(result.panUnbindingCard)
        }

        recyclerView = RecyclerView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        loadingView = LoadingView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minLoadingHeight ?: MATCH_PARENT, Gravity.CENTER)
        }

        errorView = ErrorView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minHeight ?: MATCH_PARENT, Gravity.CENTER)
            setErrorButtonText(getString(R.string.ym_retry))
        }

        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = ::showError
        )
    }

    override fun onDestroyView() {
        contentContainer.removeAllViews()
        swipeItemHelper.detachFromRecyclerView()
        super.onDestroyView()
    }

    override fun onPaymentOptionClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.ProceedWithPaymentMethod(optionId, instrumentId))
    }

    override fun onOptionsMenuClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.OpenUnbindScreen(optionId, instrumentId))
    }

    override fun onDeleteClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.OpenUnbindingAlert(optionId, instrumentId))
    }

    fun onAppear() {
        viewModel.handleAction(
            PaymentOptionList.Action.Load
        )
    }

    private fun onAuthResult(paymentAuthResult: Screen.MoneyAuth.Result) {
        viewModel.handleAction(
            when (paymentAuthResult) {
                Screen.MoneyAuth.Result.SUCCESS -> PaymentOptionList.Action.PaymentAuthSuccess
                Screen.MoneyAuth.Result.CANCEL -> PaymentOptionList.Action.PaymentAuthCancel
            }
        )
    }

    private fun onUnbindingCardResult(panUnbindingCard: String) {
        viewModel.handleAction(PaymentOptionList.Action.Load)
        view?.showSnackbar(
            message = getString(
                R.string.ym_unbinding_card_success,
                panUnbindingCard.takeLast(4)
            ),
            textColorResId = R.color.color_type_inverse,
            backgroundColorResId = R.color.color_type_success
        )
    }

    private fun showState(state: PaymentOptionList.State) {
        Picasso.get().load(Uri.parse(state.yooMoneyLogoUrl))
            .placeholder(topBar.logo.drawable)
            .into(topBar.logo)
        showState(!isTablet) {
            when (state) {
                is PaymentOptionList.State.Loading -> showLoadingState()
                is PaymentOptionList.State.Content -> showContentState(state.content)
                is PaymentOptionList.State.Error -> showErrorState(state)
                is PaymentOptionList.State.ContentWithUnbindingAlert -> showContentWithUnbindingAlert(state)
            }
        }
    }

    private fun showContentWithUnbindingAlert(state: PaymentOptionList.State.ContentWithUnbindingAlert) {
        showContentState(state.content)
        showAlert(state)
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(contentContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showAlert(state: PaymentOptionList.State.ContentWithUnbindingAlert) {
        val context = requireContext()
        AlertDialog.Builder(context, R.style.ym_DialogStyleColored)
            .setMessage(context.getString(R.string.ym_unbinding_alert_message))
            .setPositiveButton(R.string.ym_unbind_card_action) { dialog, _ ->
                actionOnDialog(
                    dialog,
                    PaymentOptionList.Action.ClickOnUnbind(state.optionId, state.instrumentId)
                )
            }
            .setNegativeButton(R.string.ym_logout_dialog_button_negative) { dialog, _ ->
                actionOnDialog(dialog, PaymentOptionList.Action.ClickOnCancel)
            }
            .show()
    }

    private fun actionOnDialog(dialog: DialogInterface, action: PaymentOptionList.Action) {
        dialog.dismiss()
        swipeItemHelper.forceCancel()
        viewModel.handleAction(action)
    }

    private fun showLoadingState() {
        topBar.isLogoVisible = uiParameters.showLogo
        replaceDynamicView(loadingView)
    }

    private fun showContentState(content: PaymentOptionListOutputModel) {
        when (content) {
            is PaymentOptionListSuccessOutputModel -> showPaymentOptions(content)
            is PaymentOptionListNoWalletOutputModel -> showAuthNoWalletViewModel()
        }
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = contentContainer.getViewHeight() }
    }

    //todo
    private fun showPaymentOptions(content: PaymentOptionListSuccessOutputModel) {
        val listItems: List<PaymentOptionListItem> = content.options.map {
            it.getPaymentOptionListItems(requireContext())
        }.flatten()

        topBar.isLogoVisible = uiParameters.showLogo
        replaceDynamicView(recyclerView)
        recyclerView.adapter = PaymentOptionListRecyclerViewAdapter(this, listItems)
        swipeItemHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAuthNoWalletViewModel() {
        if (!isStateSaved) {
            val content = YmAlertDialog.DialogContent(
                content = getString(
                    R.string.ym_no_wallet_dialog_message
                ),
                actionPositiveText = getString(R.string.ym_no_wallet_dialog_shoose_payment_option)
            )

            CheckoutAlertDialog.create(
                manager = childFragmentManager,
                content = content,
                shouldColorPositiveColor = true,
                dimAmount = 0.6f
            ).apply {
                attachListener(object : YmAlertDialog.DialogListener {
                    override fun onPositiveClick() {
                        viewModel.handleAction(PaymentOptionList.Action.Logout)
                    }
                })
            }.show(childFragmentManager)
        }
    }

    private fun showErrorState(state: PaymentOptionList.State.Error) {
        topBar.isLogoVisible = uiParameters.showLogo
        showError(state.error)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = contentContainer.getViewHeight() }
    }

    private fun showError(throwable: Throwable) {
        replaceDynamicView(errorView)
        errorView.setErrorText(errorFormatter.format(throwable))
        errorView.setErrorButtonListener(View.OnClickListener {
            viewModel.handleAction(
                PaymentOptionList.Action.Load
            )
        })
    }

    private fun showEffect(effect: PaymentOptionList.Effect) {
        when (effect) {
            is PaymentOptionList.Effect.ShowContract -> router.navigateTo(Screen.Contract)
            is PaymentOptionList.Effect.StartTokenization -> router.navigateTo(Screen.Tokenize(effect.tokenizeInputModel))
            PaymentOptionList.Effect.RequireAuth -> {
                showLoadingState()
                router.navigateTo(Screen.MoneyAuth)
            }
            is PaymentOptionList.Effect.Cancel -> router.navigateTo(Screen.TokenizeCancelled)
            is PaymentOptionList.Effect.UnbindLinkedCard -> router.navigateTo(Screen.UnbindLinkedCard(effect.paymentOption))
            is PaymentOptionList.Effect.UnbindInstrument -> router.navigateTo(Screen.UnbindInstrument(effect.instrumentBankCard))
            is PaymentOptionList.Effect.UnbindFailed -> showSnackBar(effect.instrumentBankCard, false)
            is PaymentOptionList.Effect.UnbindSuccess -> showSnackBar(effect.instrumentBankCard, true)
        }
    }

    private fun showSnackBar(instrumentBankCard: PaymentInstrumentBankCard, isUnbindSuccess: Boolean) {
        if (isUnbindSuccess) {
            view?.showSnackbar(
                message = getString(
                    R.string.ym_unbinding_card_success,
                    instrumentBankCard.last4
                ),
                textColorResId = R.color.color_type_inverse,
                backgroundColorResId = R.color.color_type_success
            )
        } else {
            view?.showSnackbar(
                message = getString(R.string.ym_unbinding_card_failed, instrumentBankCard.last4),
                textColorResId = R.color.color_type_inverse,
                backgroundColorResId = R.color.color_type_alert
            )
        }
    }

    private fun replaceDynamicView(view: View) {
        contentContainer.getChildAt(0)?.also {
            if (it === view) {
                return
            }
            contentContainer.removeView(it)
        }
        contentContainer.addView(view)
    }

    companion object {
        private const val MENU_ITEM_COUNT = 1
    }
}

private fun PaymentOption.getPaymentOptionListItems(context: Context): List<PaymentOptionListItem> {
    val instruments = (this as? BankCardPaymentOption)?.getInstrumentListItems(context) ?: emptyList()
    return instruments + PaymentOptionListItem(
        optionId = id,
        additionalInfo = getAdditionalInfo(context).let { info ->
            info?.takeIf { _ -> this is Wallet }
                ?.makeStartMedium(context) ?: info
        },
        canLogout = this is Wallet,
        hasOptions = this is LinkedCard,
        isWalletLinked = this is LinkedCard && this.isLinkedToWallet,
        title = this.title ?: getPlaceholderTitle(context),
        urlLogo = this.icon,
        logo = getPlaceholderIcon(context)
    )
}

private fun BankCardPaymentOption.getInstrumentListItems(context: Context): List<PaymentOptionListItem> {
    return paymentInstruments.map { paymentInstrument ->
        PaymentOptionListItem(
            optionId = id,
            instrumentId = paymentInstrument.paymentInstrumentId,
            additionalInfo = context.resources.getString(R.string.ym_linked_not_wallet_card),
            canLogout = false,
            hasOptions = true,
            isWalletLinked = false,
            logo = requireNotNull(
                ContextCompat.getDrawable(
                    context,
                    getBankOrBrandLogo(paymentInstrument.cardNumber, paymentInstrument.cardType)
                )
            ),
            title = "•••• " + paymentInstrument.last4,
            urlLogo = null
        )
    }
}

private fun CharSequence.makeStartMedium(context: Context) =
    (this as? Spannable ?: SpannableStringBuilder(this)).apply {
        setSpan(
            TextAppearanceSpan(context, R.style.Text_Caption1_Medium),
            0,
            length - 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }