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

package ru.yoomoney.sdk.kassa.payments.tokenize

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.ym_fragment_tokenize.errorView
import kotlinx.android.synthetic.main.ym_fragment_tokenize.loadingView
import kotlinx.android.synthetic.main.ym_fragment_tokenize.rootContainer
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment.Companion.PAYMENT_AUTH_RESULT_EXTRA
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment.Companion.PAYMENT_AUTH_RESULT_KEY
import ru.yoomoney.sdk.kassa.payments.tokenize.di.TokenizeModule
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias TokenizeViewModel = RuntimeViewModel<Tokenize.State, Tokenize.Action, Tokenize.Effect>

internal class TokenizeFragment : Fragment(R.layout.ym_fragment_tokenize) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    private val viewModel: TokenizeViewModel by viewModel(TokenizeModule.TOKENIZE) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectTokenizeFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokenizeInputModel = requireNotNull(arguments?.getParcelable<TokenizeInputModel>("tokenizeInputModel"))
        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = { error ->
                showError(error) {
                    viewModel.handleAction(Tokenize.Action.Tokenize(tokenizeInputModel))
                }
            }
        )
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            rootContainer.hideSoftKeyboard()
            parentFragmentManager.popBackStack()
            finishWithCancel()
        }
        setFragmentResultListener(PAYMENT_AUTH_RESULT_KEY) { _, bundle ->
            paymentAuthResult(
                bundle.getSerializable(PAYMENT_AUTH_RESULT_EXTRA) as Screen.PaymentAuth.PaymentAuthResult
            )
        }
        viewModel.handleAction(Tokenize.Action.Tokenize(tokenizeInputModel))
    }

    private fun paymentAuthResult(result: Screen.PaymentAuth.PaymentAuthResult) {
        when (result) {
            Screen.PaymentAuth.PaymentAuthResult.SUCCESS -> viewModel.handleAction(Tokenize.Action.PaymentAuthSuccess)
            Screen.PaymentAuth.PaymentAuthResult.CANCEL -> viewModel.handleAction(Tokenize.Action.PaymentAuthCancel)
        }
    }

    private fun showError(throwable: Throwable, action: () -> Unit) {
        errorView.setErrorText(errorFormatter.format(throwable))
        errorView.setErrorButtonListener(action)
        rootContainer.showChild(errorView)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.getViewHeight() }
    }

    private fun showLoadingState() {
        rootContainer.showChild(loadingView)
    }

    private fun showState(state: Tokenize.State) = when (state) {
        is Tokenize.State.Start -> Unit
        is Tokenize.State.Tokenize -> showLoadingState()
        is Tokenize.State.TokenizeError -> showError(state.error) {
            viewModel.handleAction(Tokenize.Action.Tokenize(state.tokenizeInputModel))
        }
    }

    private fun showEffect(effect: Tokenize.Effect) {
        when (effect) {
            is Tokenize.Effect.PaymentAuthRequired -> router.navigateTo(
                Screen.PaymentAuth(effect.charge, effect.allowWalletLinking)
            )
            is Tokenize.Effect.TokenizeComplete -> router.navigateTo(Screen.TokenizeSuccessful(effect.tokenizeOutputModel))
            is Tokenize.Effect.CancelTokenize -> finishWithCancel()
        }
    }

    private fun finishWithCancel() {
        setFragmentResult(TOKENIZE_RESULT_KEY, bundleOf(TOKENIZE_RESULT_EXTRA to Screen.Tokenize.TokenizeResult.CANCEL))
        parentFragmentManager.popBackStack()
        rootContainer.hideSoftKeyboard()
    }

    companion object {
        const val TOKENIZE_RESULT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.TOKENIZE_RESULT_KEY"
        const val TOKENIZE_RESULT_EXTRA = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.TOKENIZE_RESULT_EXTRA"

        fun newInstance(tokenizeInputModel: TokenizeInputModel): TokenizeFragment {
            return TokenizeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("tokenizeInputModel", tokenizeInputModel)
                }
            }
        }
    }
}