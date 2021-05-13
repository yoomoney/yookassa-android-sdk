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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.codeConfirmError
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.confirmCode
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.contentView
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.errorView
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.loadingView
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.retryAction
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.rootContainer
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.titleView
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.topBar
import kotlinx.android.synthetic.main.ym_fragment_payment_auth.touchInterceptor
import org.threeten.bp.Duration
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.gui.utils.extensions.show
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.extensions.showSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.toHint
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Action
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Effect
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.State
import ru.yoomoney.sdk.kassa.payments.paymentAuth.di.PaymentAuthModule.Companion.PAYMENT_AUTH
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject


internal typealias PaymentAuthViewModel = RuntimeViewModel<State, Action, Effect>

internal class PaymentAuthFragment : Fragment(R.layout.ym_fragment_payment_auth) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    private var timer: CountDownTimer? = null

    private val viewModel: PaymentAuthViewModel by viewModel(PAYMENT_AUTH) { viewModelFactory }

    private val amount: Amount by lazy {
        arguments?.getParcelable(AMOUNT_KEY) as? Amount ?: throw IllegalStateException("AMOUNT_KEY should be passed")
    }

    private val linkWalletToAp: Boolean by lazy {
        arguments?.getBoolean(LINK_WALLET_KEY) ?: throw IllegalStateException("LINK_WALLET_KEY should be passed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectPaymentAuthFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {
                showError(it) {
                    viewModel.handleAction(Action.Start(linkWalletToAp, amount))
                }
            }
        )
        viewModel.handleAction(Action.Start(linkWalletToAp, amount))
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun setupViews() {
        topBar.title = " "
        topBar.onBackButton {
            finishWithResult(Screen.PaymentAuth.PaymentAuthResult.CANCEL)
        }
    }

    private fun showState(state: State) {
        when (state) {
            is State.Loading -> showProgress()
            is State.StartError -> showError(state.error) {
                viewModel.handleAction(Action.Start(linkWalletToAp, amount))
            }
            is State.InputCode -> showInputCode(state)
            is State.InputCodeProcess -> showInputCodeProcess(state)
            is State.InputCodeVerifyExceeded -> showInputCodeVerifyExceeded(state)
            is State.ProcessError -> showError(state.error) {
                viewModel.handleAction(Action.StartSuccess(state.data))
            }
        }
    }

    private fun showProgress() {
        rootContainer.showChild(loadingView)
    }

    private fun showInputCode(inputCode: State.InputCode) {
        showInputCode(inputCode.data, null)
        startTimer(Duration.ofSeconds(inputCode.data.nextSessionTimeLeft.toLong()))
        retryAction.showProgress(false)
        touchInterceptor.hide()
        retryAction.setOnClickListener {
            codeConfirmError.text = ""
            viewModel.handleAction(Action.Start(linkWalletToAp, amount))
        }
    }

    private fun showInputCodeProcess(inputCodeProcess: State.InputCodeProcess) {
        codeConfirmError.text = ""
        showInputCode(inputCodeProcess.data, inputCodeProcess.passphrase)
        retryAction.showProgress(true)
        touchInterceptor.show()
    }

    private fun showInputCode(data: AuthTypeState.SMS, passphrase: String?) {
        rootContainer.showChild(contentView)
        titleView.text = data.type.toHint(requireContext())
        confirmCode.isEnabled = true
        confirmCode.maxLength = data.codeLength
        confirmCode.isFocusable = true
        confirmCode.isFocusableInTouchMode = true
        confirmCode.requestFocus()
        confirmCode.showSoftKeyboard()
        passphrase?.let { confirmCode.value = it }
        confirmCode.onValueChangedListener = { value ->
            codeConfirmError.text = ""
            if (value.length == data.codeLength) {
                confirmCode.onValueChangedListener = null
                viewModel.handleAction(Action.ProcessAuthRequired(value, true))
            }
        }
    }

    private fun showInputCodeVerifyExceeded(inputCodeProcess: State.InputCodeVerifyExceeded) {
        val data = inputCodeProcess.data
        val passphrase = inputCodeProcess.passphrase
        rootContainer.showChild(contentView)
        codeConfirmError.text = getString(R.string.ym_payment_auth_no_attempts)
        confirmCode.onValueChangedListener = null
        titleView.text = data.type.toHint(requireContext())
        confirmCode.isEnabled = false
        confirmCode.maxLength = data.codeLength
        confirmCode.value = passphrase
        confirmCode.isFocusable = false
        confirmCode.isFocusableInTouchMode = false
        confirmCode.hideSoftKeyboard()
        retryAction.showProgress(false)
        touchInterceptor.hide()
    }

    private fun showEffect(effect: Effect) = when (effect) {
        is Effect.ProcessAuthWrongAnswer -> {
            codeConfirmError.show()
            val text = if (effect.attemptsCount != null && effect.attemptsLeft != null) {
                when (effect.attemptsLeft) {
                    effect.attemptsCount - 1 -> getString(R.string.ym_payment_auth_wrong_code_try_again)
                    0 -> getString(R.string.ym_payment_auth_no_attempts)
                    else -> getString(R.string.ym_payment_auth_wrong_code_with_attempts, effect.attemptsLeft)
                }
            } else {
                getString(R.string.ym_payment_auth_wrong_code)
            }
            codeConfirmError.text = text
        }
        is Effect.ShowSuccess -> finishWithResult(Screen.PaymentAuth.PaymentAuthResult.SUCCESS)
    }

    private fun showError(throwable: Throwable, action: () -> Unit) {
        errorView.setErrorText(errorFormatter.format(throwable))
        errorView.setErrorButtonListener(View.OnClickListener { action() })
        rootContainer.showChild(errorView)
    }

    private fun finishWithResult(paymentAuthResult: Screen.PaymentAuth.PaymentAuthResult) {
        router.navigateTo(Screen.Contract(paymentAuthResult))
    }

    private fun startTimer(duration: Duration) {
        timer?.cancel()
        retryAction.isEnabled = false
        timer = object : CountDownTimer(duration.toMillis(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isVisible) {
                    val secondsNow = millisUntilFinished / 1000
                    val minutes = (secondsNow / 60).toTimeString()
                    val seconds = (secondsNow % 60).toTimeString()
                    retryAction.text = getString(R.string.ym_confirm_retry_timer_text, "$minutes:$seconds")
                }
            }

            override fun onFinish() {
                retryAction.text = getString(R.string.ym_payment_auth_retry_text)
                retryAction.isEnabled = true
            }
        }.start()
    }

    companion object {

        private const val AMOUNT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.AMOUNT_KEY"
        private const val LINK_WALLET_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.LINK_WALLET_KEY"

        fun createFragment(amount: Amount, linkWalletToApp: Boolean): PaymentAuthFragment {
            return PaymentAuthFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(AMOUNT_KEY, amount)
                    putBoolean(LINK_WALLET_KEY, linkWalletToApp)
                }
            }
        }
    }
}

internal fun Long.toTimeString(): String {
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}