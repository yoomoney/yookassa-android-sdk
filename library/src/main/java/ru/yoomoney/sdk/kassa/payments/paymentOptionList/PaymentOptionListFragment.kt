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

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.ym_fragment_payment_options.contentContainer
import kotlinx.android.synthetic.main.ym_fragment_payment_options.topBar
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodId
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsListFormatter
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsModule.Companion.PAYMENT_OPTIONS
import ru.yoomoney.sdk.kassa.payments.extensions.getAdditionalInfo
import ru.yoomoney.sdk.kassa.payments.extensions.getIcon
import ru.yoomoney.sdk.kassa.payments.extensions.getTitle
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.view.ErrorView
import ru.yoomoney.sdk.kassa.payments.ui.view.LoadingView
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutAlertDialog
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias PaymentOptionListViewModel = RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>

internal class PaymentOptionListFragment : Fragment(R.layout.ym_fragment_payment_options),
    PaymentOptionListRecyclerViewAdapter.PaymentOptionClickListener {

    @Inject
    lateinit var paymentParameters: PaymentParameters

    @Inject
    lateinit var uiParameters: UiParameters

    @Inject
    @JvmField
    var paymentMethodId: PaymentMethodId? = null

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
        val minLoadingHeight = resources.getDimensionPixelSize(R.dimen.ym_payment_options_loading_min_height).takeIf { !isTablet }

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
        super.onDestroyView()
    }

    override fun onPaymentOptionClick(optionId: Int) {
        viewModel.handleAction(PaymentOptionList.Action.ProceedWithPaymentMethod(optionId))
    }

    fun onAppear() {
        viewModel.handleAction(PaymentOptionList.Action.Load(
            paymentParameters.amount,
            paymentMethodId
        ))
    }

    fun onAuthResult(paymentAuthResult: Screen.MoneyAuth.Result) {
        viewModel.handleAction(
            when (paymentAuthResult) {
                Screen.MoneyAuth.Result.SUCCESS -> PaymentOptionList.Action.PaymentAuthSuccess
                Screen.MoneyAuth.Result.CANCEL -> PaymentOptionList.Action.PaymentAuthCancel
            }
        )
    }

    private fun showState(state: PaymentOptionList.State) {
        showState(!isTablet) {
            when (state) {
                is PaymentOptionList.State.Loading -> showLoadingState()
                is PaymentOptionList.State.Content -> showContentState(state)
                is PaymentOptionList.State.Error -> showErrorState(state)
            }
        }
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(contentContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showLoadingState() {
        topBar.isLogoVisible = uiParameters.showLogo
        replaceDynamicView(loadingView)
    }

    private fun showContentState(state: PaymentOptionList.State.Content) {
        when(val content = state.content){
            is PaymentOptionListSuccessOutputModel -> showPaymentOptions(content)
            is PaymentOptionListNoWalletOutputModel -> showAuthNoWalletViewModel()
        }
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = contentContainer.getViewHeight() }
    }

    private fun showPaymentOptions(content: PaymentOptionListSuccessOutputModel) {
        val paymentOptions = content.options.map {
            PaymentOptionListItem(
                optionId = it.id,
                icon = it.getIcon(requireContext()),
                title = it.getTitle(requireContext()),
                additionalInfo = it.getAdditionalInfo().let { info ->
                    info?.takeIf { _ -> it is Wallet }
                        ?.makeStartMedium() ?: info
                },
                canLogout = it is Wallet
            )
        }
        if (content.options.size == 1) {
            viewModel.handleAction(PaymentOptionList.Action.ProceedWithPaymentMethod(content.options.first().id))
        } else {
            topBar.isLogoVisible = uiParameters.showLogo
            replaceDynamicView(recyclerView)
            recyclerView.adapter = PaymentOptionListRecyclerViewAdapter(this, paymentOptions)
        }
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
                PaymentOptionList.Action.Load(
                paymentParameters.amount,
                paymentMethodId
            ))
        })
    }

    private fun showEffect(effect: PaymentOptionList.Effect) {
        when(effect) {
            is PaymentOptionList.Effect.ProceedWithPaymentMethod -> router.navigateTo(Screen.Contract())
            PaymentOptionList.Effect.RequireAuth -> {
                showLoadingState()
                router.navigateTo(Screen.MoneyAuth)
            }
            PaymentOptionList.Effect.Cancel -> router.navigateTo(Screen.TokenizeCancelled)
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

    private fun CharSequence.makeStartMedium() =
        (this as? Spannable ?: SpannableStringBuilder(this)).apply {
            setSpan(TextAppearanceSpan(requireContext(), R.style.Text_Caption1_Medium), 0, length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}
