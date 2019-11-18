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

package ru.yandex.money.android.sdk.impl

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import android.widget.FrameLayout
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.contract.ContractCompleteViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFragment
import ru.yandex.money.android.sdk.impl.contract.ContractViewModel
import ru.yandex.money.android.sdk.impl.extensions.hideSoftKeyboard
import ru.yandex.money.android.sdk.impl.extensions.inTransaction
import ru.yandex.money.android.sdk.impl.logout.LogoutFailViewModel
import ru.yandex.money.android.sdk.impl.logout.LogoutSuccessViewModel
import ru.yandex.money.android.sdk.impl.logout.LogoutViewModel
import ru.yandex.money.android.sdk.impl.payment.tokenize.BankCardDialogFragment
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.PaymentOptionInfoViewModel
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListCloseViewModel
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListFragment
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthFailViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthSuccessViewModel
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthFragment
import ru.yandex.money.android.sdk.model.UnhandledException
import ru.yandex.money.android.sdk.model.ViewModel
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionInputModel

private const val PAYMENT_OPTION_LIST_FRAGMENT_TAG = "paymentOptionListFragment"
private const val CONTRACT_FRAGMENT_TAG = "contractFragment"
private const val BANK_CARD_FRAGMENT_TAG = "bankCardFragment"
private const val AUTH_FRAGMENT_TAG = "authFragment"

internal class MainDialogFragment : BottomSheetDialogFragment() {

    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private val paymentOptionInfoListener: (ViewModel) -> Unit = {
        if (it is PaymentOptionInfoViewModel) {
            showBankCardDialog()
        } else {
            hideBankCardDialog()
        }
    }

    private val paymentOptionListListener: (PaymentOptionListViewModel) -> Unit = {
        if (it === PaymentOptionListCloseViewModel) {
            dismiss()
        } else {
            if (isHidden) {
                fragmentManager?.takeIf { !isStateSaved && isAdded }?.popBackStack()
            } else {
                childFragmentManager.takeIf { !isStateSaved && isAdded }?.popBackStack()
            }
        }
    }

    private val selectPaymentOptionListener: (ContractViewModel) -> Unit = { viewModel ->
        view?.post {
            if (!isStateSaved && isAdded) {
                childFragmentManager.also {
                    val contract = it.findFragmentByTag(CONTRACT_FRAGMENT_TAG)!!
                    if (!contract.isHidden) {
                        return@also
                    }
                    it.inTransaction {
                        hide(it.findFragmentByTag(PAYMENT_OPTION_LIST_FRAGMENT_TAG)!!)
                        show(contract)
                        addToBackStack(null)
                    }
                }
            }
        }
    }

    private val successTokenizeListener: (ContractCompleteViewModel) -> Unit = {
        dismissAllowingStateLoss()
    }

    private val authListener: (UserAuthSuccessViewModel) -> Unit = {
        childFragmentManager.takeIf { !isStateSaved && isAdded }?.popBackStack()
        AppModel.loadPaymentOptionListController.retry()
    }

    private val showAuthFail: (UserAuthFailViewModel) -> Unit = {
        childFragmentManager.takeIf { !isStateSaved && isAdded }?.popBackStack()
    }

    private val logoutListener: (LogoutViewModel) -> Unit = {
        if (it is LogoutSuccessViewModel || it is LogoutFailViewModel) {
            childFragmentManager.takeIf { !isStateSaved && isAdded }?.popBackStack()
            AppModel.loadPaymentOptionListController.retry()
        }
    }

    private val unhandledExceptionListener: (UnhandledException) -> Unit = {
        childFragmentManager.takeIf { !isStateSaved && isAdded }?.popBackStack()
    }

    init {
        setStyle(STYLE_NO_FRAME, R.style.ym_MainDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = checkNotNull(context).let {
        val isTablet = it.resources.getBoolean(R.bool.ym_isTablet)
        if (isTablet) {
            BackPressedAppCompatDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
            }
        } else {
            BackPressedBottomSheetDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ym_fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = view.resources.getBoolean(R.bool.ym_isTablet)

        if (savedInstanceState == null) {
            childFragmentManager.inTransaction {
                add(R.id.containerBottomSheet, PaymentOptionListFragment(), PAYMENT_OPTION_LIST_FRAGMENT_TAG)

                if (AppModel.yandexAuthGateway != null) {
                    add(R.id.containerBottomSheet, YandexAuthFragment(), AUTH_FRAGMENT_TAG)
                }

                ContractFragment().also {
                    add(R.id.containerBottomSheet, it, CONTRACT_FRAGMENT_TAG)
                    hide(it)
                }

                if (isTablet) {
                    BankCardDialogFragment().also {
                        add(R.id.containerBottomSheet, it, BANK_CARD_FRAGMENT_TAG)
                        hide(it)
                    }
                }
            }
        }

        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            (dialog as? BottomSheetDialog)?.also { dialog ->
                val bottomSheet = dialog.findViewById<FrameLayout?>(android.support.design.R.id.design_bottom_sheet)
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    peekHeight = 0
                }
            }
        }
        view.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)

        AppModel.listeners += paymentOptionInfoListener
        AppModel.listeners += paymentOptionListListener
        AppModel.listeners += selectPaymentOptionListener
        AppModel.listeners += successTokenizeListener
        AppModel.listeners += authListener
        AppModel.listeners += showAuthFail
        AppModel.listeners += logoutListener
        AppModel.listeners += unhandledExceptionListener

        (dialog as? WithBackPressedListener)?.onBackPressed = {
            val contractShown =
                isHidden || childFragmentManager.findFragmentByTag(CONTRACT_FRAGMENT_TAG)?.isHidden == false
            val bankCardShown = childFragmentManager.findFragmentByTag(BANK_CARD_FRAGMENT_TAG)?.isHidden == false

            if (contractShown) {
                AppModel.changePaymentOptionController(ChangePaymentOptionInputModel)
            }
            if (bankCardShown) {
                AppModel.selectPaymentOptionController.retry()
            }

            contractShown || bankCardShown
        }
    }

    override fun onResume() {
        super.onResume()
        AppModel.sessionReporter.resumeSession()
    }

    override fun onPause() {
        AppModel.sessionReporter.pauseSession()
        super.onPause()
    }

    override fun onDestroyView() {
        AppModel.listeners -= paymentOptionInfoListener
        AppModel.listeners -= paymentOptionListListener
        AppModel.listeners -= selectPaymentOptionListener
        AppModel.listeners -= successTokenizeListener
        AppModel.listeners -= authListener
        AppModel.listeners -= showAuthFail
        AppModel.listeners -= logoutListener
        AppModel.listeners -= unhandledExceptionListener

        onGlobalLayoutListener?.also {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                @Suppress("DEPRECATION")
                view?.viewTreeObserver?.removeGlobalOnLayoutListener(it)
            } else {
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(it)
            }
        }

        (dialog as? WithBackPressedListener)?.onBackPressed = null

        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        AppModel.reset()
        activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        view?.hideSoftKeyboard()

        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun hideBankCardDialog() {
        context?.resources?.also {
            val bankCardFragment = childFragmentManager.findFragmentByTag(BANK_CARD_FRAGMENT_TAG)
            if (it.getBoolean(R.bool.ym_isTablet)) {
                if (bankCardFragment?.isHidden == false) {
                    childFragmentManager.popBackStack()
                }
            } else {
                (bankCardFragment as BankCardDialogFragment?)?.dismissAllowingStateLoss()
            }
        }
    }

    private val showBankCardScreen = Runnable {
        if (!isStateSaved && isAdded) {
            context?.resources?.also {
                val isTablet = it.getBoolean(R.bool.ym_isTablet)
                val tokenize = childFragmentManager.findFragmentByTag(BANK_CARD_FRAGMENT_TAG)
                if (isTablet && tokenize != null) {
                    if (tokenize.isHidden) {
                        childFragmentManager.also {
                            it.inTransaction {
                                hide(it.findFragmentByTag(CONTRACT_FRAGMENT_TAG)!!)
                                show(tokenize)
                                addToBackStack(null)
                            }
                        }
                    }
                } else {
                    if (tokenize == null) {
                        BankCardDialogFragment().show(childFragmentManager, BANK_CARD_FRAGMENT_TAG)
                    }
                }
            }
        }
    }

    private fun showBankCardDialog() {
        view?.apply {
            removeCallbacks(showBankCardScreen)
            post(showBankCardScreen)
        }
    }
}
