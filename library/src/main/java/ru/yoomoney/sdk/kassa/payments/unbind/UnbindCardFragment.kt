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

package ru.yoomoney.sdk.kassa.payments.unbind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewPropertyAnimator
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.ym_fragment_unbind_card.bankCardView
import kotlinx.android.synthetic.main.ym_fragment_unbind_card.informerView
import kotlinx.android.synthetic.main.ym_fragment_unbind_card.topBar
import kotlinx.android.synthetic.main.ym_fragment_unbind_card.unbindCardButton
import kotlinx.android.synthetic.main.ym_fragment_unbind_card.rootContainer
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.contract.SavePaymentMethodInfoActivity
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.extensions.showSnackbar
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.resumePostponedTransition
import ru.yoomoney.sdk.kassa.payments.unbind.di.UnbindCardModule
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias UntieCardViewModel = RuntimeViewModel<UnbindCard.State, UnbindCard.Action, UnbindCard.Effect>

internal class UnbindCardFragment : Fragment(R.layout.ym_fragment_unbind_card) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var reporter: Reporter

    private val topBarAnimator: ViewPropertyAnimator? by lazy { topBar?.animate() }

    private val viewModel: UntieCardViewModel by viewModel(UnbindCardModule.UNBIND_CARD) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectUntieCardFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isTablet) {
            postponeEnterTransition()
        }
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            closeScreen()
        }
        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {}
        )

        val linkedCard = arguments?.getParcelable<LinkedCard>(PAYMENT_OPTION_LINKED_CARD)
        val instrumental = arguments?.getParcelable<PaymentInstrumentBankCard>(PAYMENT_OPTION_INSTRUMENT)

        viewModel.handleAction(UnbindCard.Action.StartDisplayData(linkedCard, instrumental))
        if (isTablet) {
            rootContainer.updateLayoutParams<ViewGroup.LayoutParams> { height = resources.getDimensionPixelSize(R.dimen.ym_dialogHeight) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        topBarAnimator?.cancel()
    }

    private fun setupToolbar(pan: String) {
        topBar.title = "•••• $pan"
        topBar.onBackButton { closeScreen() }
    }

    private fun setUpCardForm(pan: String, name: String) {
        with(bankCardView) {
            setChangeCardAvailable(false)
            showBankLogo(pan)
            setCardData(
                cardNumber = pan,
                cardName = name
            )
            hideAdditionalInfo()
        }
    }

    private fun setupText(isWalletLinked: Boolean) {
        if (isWalletLinked) {
            informerView.messageText = getString(R.string.ym_informer_unbind_card_wallet)
        } else {
            informerView.messageText = getString(R.string.ym_informer_unbind_text)
        }
        informerView.actionText = getString(R.string.ym_informer_unbind_action)
    }

    private fun setupButton(cardId: String, isWalletLinked: Boolean) {
        unbindCardButton.setOnClickListener {
            viewModel.handleAction(UnbindCard.Action.StartUnbinding(cardId))
        }

        unbindCardButton.isVisible = !isWalletLinked
    }

    private fun showEffect(effect: UnbindCard.Effect) {
        when (effect) {
            is UnbindCard.Effect.UnbindComplete -> {
                setFragmentResult(
                    UNBIND_CARD_RESULT_KEY, bundleOf(
                        UNBIND_CARD_RESULT_EXTRA to Screen.UnbindInstrument.Success(
                            effect.instrumentBankCard.last4
                        )
                    )
                )
                closeScreen()
            }
            is UnbindCard.Effect.UnbindFailed -> {
                unbindCardButton.showProgress(false)
                view?.showSnackbar(
                    message = getString(R.string.ym_unbinding_card_failed, effect.instrumentBankCard.last4),
                    textColorResId = R.color.color_type_inverse,
                    backgroundColorResId = R.color.color_type_alert
                )
            }
        }
    }

    private fun showState(state: UnbindCard.State) {
        when (state) {
            is UnbindCard.State.Initial -> Unit
            is UnbindCard.State.ContentLinkedBankCard -> showContentLinkedBankCard(state)
            is UnbindCard.State.LoadingUnbinding -> unbindCardButton.showProgress(true)
            is UnbindCard.State.ContentLinkedWallet -> showContentLinkedWallet(state)
        }
    }

    private fun showContentLinkedBankCard(state: UnbindCard.State.ContentLinkedBankCard) {
        resumePostponedViewSetup()
        val instrumentBankCard = state.instrumentBankCard
        val cardNumber = "${instrumentBankCard.first6}••••••${instrumentBankCard.last4}"
        setUpCardForm(cardNumber, getString(R.string.ym_linked_card))
        setupButton(instrumentBankCard.paymentInstrumentId, false)
        setupToolbar(instrumentBankCard.last4)
        setupText(false)
        informerView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.ym_informer_linked_card_height)
        }
        informerView.setActionClickListener(
            listener = setupInformerViewAction(
                R.string.ym_how_works_auto_write_title,
                R.string.ym_how_works_auto_write_body,
                false
            )
        )
    }

    private fun showContentLinkedWallet(state: UnbindCard.State.ContentLinkedWallet) {
        resumePostponedViewSetup()
        val linkedCard = state.linkedCard
        setUpCardForm(linkedCard.pan, getString(R.string.ym_linked_wallet_card))
        setupButton(linkedCard.cardId, true)
        setupToolbar(linkedCard.pan.takeLast(4))
        setupText(true)
        informerView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.ym_informer_wallet_linked_card_height)
        }
        informerView.setActionClickListener(
            listener = setupInformerViewAction(
                R.string.ym_how_unbind_wallet_card_title,
                R.string.ym_how_unbind_wallet_card_body,
                true
            )
        )
    }

    private fun resumePostponedViewSetup() {
        rootContainer.updateLayoutParams<ViewGroup.LayoutParams> { height = MATCH_PARENT }
        resumePostponedTransition(rootContainer)
    }

    private fun setupInformerViewAction(
        titleRes: Int,
        textRes: Int,
        isMetricShouldSend: Boolean
    ) = View.OnClickListener {
        if (isMetricShouldSend) {
            reporter.report("screenDetailsUnbindWalletCard", null)
        }
        ContextCompat.startActivity(
            requireContext(),
            SavePaymentMethodInfoActivity.create(
                requireContext(), titleRes, textRes, null
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null
        )
    }

    private fun closeScreen() {
        parentFragmentManager.popBackStack()
        router.navigateTo(Screen.PaymentOptions())
    }

    companion object {
        const val UNBIND_CARD_RESULT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.UNBIND_CARD_RESULT_KEY"
        const val UNBIND_CARD_RESULT_EXTRA = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.UNBIND_CARD_RESULT_EXTRA"

        private const val PAYMENT_OPTION_LINKED_CARD =
            "ru.yoomoney.sdk.kassa.payments.unbind.PAYMENT_OPTION_LINKED_CARD"
        private const val PAYMENT_OPTION_INSTRUMENT = "ru.yoomoney.sdk.kassa.payments.unbind.PAYMENT_OPTION_INSTRUMENT"

        fun createFragment(linkedCard: LinkedCard): UnbindCardFragment {
            return UnbindCardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PAYMENT_OPTION_LINKED_CARD, linkedCard)
                }
            }
        }

        fun createFragment(instrumentBankCard: PaymentInstrumentBankCard): UnbindCardFragment {
            return UnbindCardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PAYMENT_OPTION_INSTRUMENT, instrumentBankCard)
                }
            }
        }
    }
}