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

package ru.yoo.sdk.kassa.payments.ui

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.ym_fragment_bottom_sheet.topArrowLine
import ru.yoo.sdk.gui.utils.extensions.hide
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.di.CheckoutInjector
import ru.yoo.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoo.sdk.kassa.payments.extensions.inTransaction
import ru.yoo.sdk.kassa.payments.metrics.SessionReporter
import ru.yoo.sdk.kassa.payments.navigation.Router
import ru.yoo.sdk.kassa.payments.navigation.Screen
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoo.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoo.sdk.kassa.payments.model.toType
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.contract.ContractFragment
import ru.yoo.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoo.sdk.kassa.payments.ui.view.BackPressedAppCompatDialog
import ru.yoo.sdk.kassa.payments.ui.view.BackPressedBottomSheetDialog
import ru.yoo.sdk.kassa.payments.ui.view.WithBackPressedListener
import javax.inject.Inject

private const val PAYMENT_OPTION_LIST_FRAGMENT_TAG = "paymentOptionListFragment"
private const val CONTRACT_FRAGMENT_TAG = "contractFragment"
private const val AUTH_FRAGMENT_TAG = "authFragment"
private const val PAYMENT_AUTH_FRAGMENT_TAG = "paymentAuthFragment"

internal class MainDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var sessionReporter: SessionReporter

    @Inject
    lateinit var paymentParameters: PaymentParameters

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository

    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        setStyle(STYLE_NO_FRAME, R.style.ym_MainDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = requireContext().let {
        CheckoutInjector.injectMainDialogFragment(this)
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
        if (isTablet) {
            topArrowLine.hide()
        }

        if (savedInstanceState == null) {
            childFragmentManager.inTransaction {
                if (paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY)) {
                    add(R.id.authContainer,
                        MoneyAuthFragment(), AUTH_FRAGMENT_TAG
                    )
                }
                add(
                    R.id.containerBottomSheet,
                    PaymentOptionListFragment(),
                    PAYMENT_OPTION_LIST_FRAGMENT_TAG
                )
            }
        }

        router.screensData.observe(viewLifecycleOwner, Observer(::onScreenChanged))

        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            (dialog as? BottomSheetDialog)?.also { dialog ->
                val bottomSheet = dialog.findViewById<FrameLayout?>(R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    peekHeight = 0
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            }
        }
        view.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)

        (dialog as? WithBackPressedListener)?.onBackPressed = {
            val contractShown =
                isHidden || childFragmentManager.findFragmentByTag(CONTRACT_FRAGMENT_TAG)?.isHidden == false
            val paymentAuthShown = childFragmentManager.findFragmentByTag(PAYMENT_AUTH_FRAGMENT_TAG)?.isHidden == false

            when {
                paymentAuthShown -> {
                    router.navigateTo(
                        Screen.Contract(
                            Screen.PaymentAuth.PaymentAuthResult.CANCEL))
                }
                contractShown -> {
                    router.takeUnless { loadedPaymentOptionListRepository.getLoadedPaymentOptions().size == 1 }
                        ?.let {
                            childFragmentManager.popBackStack()
                            it.navigateTo(Screen.PaymentOptions())
                        }
                        ?: dismissAllowingStateLoss()
                }
            }

            contractShown || paymentAuthShown
        }

        dialog?.setOnShowListener {
            dialog?.findViewById<View>(R.id.design_bottom_sheet)?.apply {
                background = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sessionReporter.resumeSession()
    }

    override fun onPause() {
        sessionReporter.pauseSession()
        super.onPause()
    }

    override fun onDestroyView() {
        onGlobalLayoutListener?.also {
            view?.viewTreeObserver?.removeOnGlobalLayoutListener(it)
        }

        (dialog as? WithBackPressedListener)?.onBackPressed = null

        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        view?.hideSoftKeyboard()

        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun onScreenChanged(screen: Screen) {
        when (screen) {
            is Screen.Contract -> {
                with(childFragmentManager) {
                    val fragment = (findFragmentByTag(CONTRACT_FRAGMENT_TAG) as ContractFragment?)
                    if (fragment != null) {
                        (findFragmentByTag(PAYMENT_AUTH_FRAGMENT_TAG) as? PaymentAuthFragment)?.let {
                            popBackStackImmediate()
                        }
                        if (screen.paymentAuthResult != null) {
                            fragment.paymentAuthResult(screen.paymentAuthResult)
                        } else {
                            fragment.takeUnless { it.isHidden }?.reload()
                        }
                    } else {
                        transitToFragment(
                            findFragmentByTag(PAYMENT_OPTION_LIST_FRAGMENT_TAG)!!,
                            ContractFragment(), CONTRACT_FRAGMENT_TAG
                        )
                    }
                }
            }
            is Screen.PaymentOptions -> {
                with(childFragmentManager) {
                    val paymentOptionListFragment =
                        (findFragmentByTag(PAYMENT_OPTION_LIST_FRAGMENT_TAG) as PaymentOptionListFragment)

                    if (paymentOptionListFragment.isHidden) {
                        takeIf { !isStateSaved && isAdded }
                            ?.popBackStack()
                    }

                    if (screen.moneyAuthResult != null) {
                        paymentOptionListFragment.onAuthResult(screen.moneyAuthResult)
                    } else {
                        paymentOptionListFragment.onAppear()
                    }
                }
            }
            is Screen.MoneyAuth -> {
                (childFragmentManager.findFragmentByTag(AUTH_FRAGMENT_TAG) as MoneyAuthFragment).authorize()
            }
            is Screen.TokenizeSuccessful -> {
                val result = Intent()
                    .putExtra(EXTRA_PAYMENT_TOKEN, screen.tokenOutputModel.token)
                    .putExtra(EXTRA_PAYMENT_METHOD_TYPE, screen.tokenOutputModel.option.toType())

                with(requireActivity()) {
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
            is Screen.TokenizeCancelled -> {
                with(requireActivity()) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            is Screen.PaymentAuth -> {
                transitToFragment(
                    childFragmentManager.findFragmentByTag(CONTRACT_FRAGMENT_TAG)!!,
                    PaymentAuthFragment.createFragment(screen.amount, screen.linkWalletToApp),
                    PAYMENT_AUTH_FRAGMENT_TAG
                )
            }
        }
    }

    private fun transitToFragment(fromFragment: Fragment, newFragment: Fragment, tag: String) {
        val currentFragmentRoot = fromFragment.requireView()
        childFragmentManager
            .beginTransaction()
            .apply {
                if (!isTablet) {
                    addSharedElement(currentFragmentRoot, currentFragmentRoot.transitionName)
                    setReorderingAllowed(true)
                    newFragment.sharedElementEnterTransition = BottomSheetFragmentTransition()
                }
            }
            .replace(R.id.containerBottomSheet, newFragment, tag)
            .addToBackStack(newFragment::class.java.name)
            .commit()
    }
}
