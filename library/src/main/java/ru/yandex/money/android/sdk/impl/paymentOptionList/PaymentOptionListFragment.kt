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

package ru.yandex.money.android.sdk.impl.paymentOptionList

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.ym_fragment_payment_options.*
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.view.ErrorView
import ru.yandex.money.android.sdk.impl.view.LoadingView
import ru.yandex.money.android.sdk.impl.view.MaxHeightRecyclerView
import ru.yandex.money.android.sdk.impl.extensions.visible
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthCancelledViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthFailViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthNoWalletViewModel
import ru.yandex.money.android.sdk.model.UnhandledException
import ru.yandex.money.android.sdk.utils.showNoWalletDialog

internal class PaymentOptionListFragment : Fragment(), PaymentOptionListRecyclerViewAdapter.PaymentOptionClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: LoadingView
    private lateinit var errorView: ErrorView

    private val showProgress: (PaymentOptionListProgressViewModel) -> Unit = {
        logo.visible = it.showLogo
        replaceDynamicView(loadingView)
    }

    private val showPaymentOptions: (PaymentOptionListSuccessViewModel) -> Unit = {
        if (it.paymentOptions.size == 1) {
            AppModel.selectPaymentOptionController(it.paymentOptions[0].optionId)
        } else {
            logo.visible = it.showLogo
            replaceDynamicView(recyclerView)
            recyclerView.adapter = PaymentOptionListRecyclerViewAdapter(this, it.paymentOptions)
        }
    }

    private val showFail: (PaymentOptionListFailViewModel) -> Unit = {
        logo.visible = it.showLogo
        showError(it.error)
        errorView.setErrorButtonListener(View.OnClickListener {
            AppModel.loadPaymentOptionListController.retry()
        })
    }

    private val showAuthFail: (UserAuthFailViewModel) -> Unit = {
        showError(it.error)
        errorView.setErrorButtonListener(View.OnClickListener {
            AppModel.userAuthController.retry()
        })
    }

    private val showAuthNoWalletViewModel: (UserAuthNoWalletViewModel) -> Unit = {
        if (!isStateSaved) {
            showNoWalletDialog(context!!, it.accountName)
        }
    }

    private val showAuthCancelledViewModel: (UserAuthCancelledViewModel) -> Unit = {
        AppModel.changePaymentOptionController(Unit)
    }

    private val unhandledExceptionListener: (UnhandledException) -> Unit = {
        showError(getString(R.string.ym_unhandled_error))
        errorView.setErrorButtonListener(View.OnClickListener {
            AppModel.loadPaymentOptionListController.retry()
        })
    }

    private fun showError(text: CharSequence) {
        replaceDynamicView(errorView)
        errorView.setErrorText(text)
    }

    override fun onCreateView(view: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return view.inflate(R.layout.ym_fragment_payment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resources = view.resources
        val isTablet = resources.getBoolean(R.bool.ym_isTablet)
        val minHeight = resources.getDimensionPixelSize(R.dimen.ym_viewAnimator_maxHeight).takeIf { !isTablet }

        recyclerView = MaxHeightRecyclerView(view.context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            maxHeight = minHeight ?: 0
        }

        loadingView = LoadingView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minHeight ?: MATCH_PARENT, Gravity.CENTER)
        }

        errorView = ErrorView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minHeight ?: MATCH_PARENT, Gravity.CENTER)
            setErrorButtonText(getString(R.string.ym_retry))
        }

        AppModel.listeners += showProgress
        AppModel.listeners += showPaymentOptions
        AppModel.listeners += showFail
        AppModel.listeners += showAuthFail
        AppModel.listeners += showAuthNoWalletViewModel
        AppModel.listeners += showAuthCancelledViewModel
        AppModel.listeners += unhandledExceptionListener
    }

    override fun onDestroyView() {
        AppModel.listeners -= showProgress
        AppModel.listeners -= showPaymentOptions
        AppModel.listeners -= showFail
        AppModel.listeners -= showAuthFail
        AppModel.listeners -= showAuthNoWalletViewModel
        AppModel.listeners -= showAuthCancelledViewModel
        AppModel.listeners -= unhandledExceptionListener

        removeDynamicViews()

        super.onDestroyView()
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

    private fun removeDynamicViews() {
        contentContainer.removeAllViews()
    }

    override fun onPaymentOptionClick(optionId: Int) {
        AppModel.selectPaymentOptionController(optionId)
    }
}
