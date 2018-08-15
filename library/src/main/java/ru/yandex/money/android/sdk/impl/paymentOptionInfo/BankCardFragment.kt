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

package ru.yandex.money.android.sdk.impl.paymentOptionInfo

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.ImageView
import kotlinx.android.synthetic.main.ym_fragment_bank_card.*
import ru.yandex.money.android.sdk.PaymentOptionInfo
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.extensions.clear
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeInputModel
import ru.yandex.money.android.sdk.utils.PatternInputFilter
import ru.yandex.money.android.sdk.utils.SimpleTextWatcher

private const val MIN_LENGTH_CSC = 3
private const val KEY_FOCUSED_VIEW_ID = "focusedViewId"

internal abstract class BankCardFragment : Fragment() {

    var closeListener: (() -> Unit)? = null

    private var proceed: (() -> Unit)? = null

    private val paymentOptionInfoListener: (PaymentOptionInfoViewModel) -> Unit = { viewModel ->
        proceed = {
            check(isAllFieldsCorrect()) { "proceedPay should be called only when all fields is correct" }
            AppModel.tokenizeController(
                TokenizeInputModel(
                    paymentOptionId = viewModel.optionId,
                    allowRecurringPayments = viewModel.allowRecurringPayments,
                    paymentOptionInfo = collectPaymentOptionInfo()
                )
            )
        }
    }

    private val payButtonStateController = object : SimpleTextWatcher {
        override fun afterTextChanged(s: Editable) {
            payButton.isEnabled = isAllFieldsCorrect()
        }
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.ym_fragment_bank_card, container, false) as View

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        payButton.setOnClickListener { proceed?.invoke() }
        payButton.isEnabled = isAllFieldsCorrect()

        toolbar.setNavigationOnClickListener { closeListener?.invoke() }

        cardNumberEditText.addTextChangedListener(payButtonStateController)
        expiryEditText.addTextChangedListener(payButtonStateController)

        with(cscEditText) {
            addTextChangedListener(payButtonStateController)
            filters += PatternInputFilter("\\D")
            setupClearButton(cscContainer, this)
            setOnEditorActionListener { _, actionId, _ ->
                actionId == IME_ACTION_DONE && isAllFieldsCorrect() && proceed?.invoke() != null
            }
        }

        if (!isHidden && savedInstanceState?.containsKey(KEY_FOCUSED_VIEW_ID) == true) {
            view.findViewById<View>(savedInstanceState.getInt(KEY_FOCUSED_VIEW_ID)).requestFocus()
        }

        AppModel.listeners += paymentOptionInfoListener
    }

    @CallSuper
    override fun onDestroyView() {
        AppModel.listeners -= paymentOptionInfoListener

        super.onDestroyView()
    }


    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.findFocus()?.id?.also { outState.putInt(KEY_FOCUSED_VIEW_ID, it) }
    }

    protected fun setTitle(text: CharSequence) {
        if (appbar == null) {
            toolbar.title = text
        } else {
            appbar?.title = text
        }
    }

    protected abstract fun collectPaymentOptionInfo(): PaymentOptionInfo

    protected abstract fun isCardNumberCorrect(): Boolean

    protected abstract fun isExpiryCorrect(): Boolean

    protected fun setupClearButton(container: ViewGroup, editText: EditText) {
        setupClearAndActionButton(container, editText, null, null)
    }

    protected fun setupClearAndActionButton(
        container: ViewGroup,
        editText: EditText,
        @DrawableRes icon: Int?,
        onClick: (() -> Unit)?
    ) {
        val clearButton = createImageButtonForEdittext(container, R.id.ym_clear_action_button, R.drawable.ym_ic_close) {
            editText.clear()
            editText.requestFocus()
        }
        val actionButton = if (icon != null && onClick != null) {
            createImageButtonForEdittext(container, R.id.ym_other_action_button, icon, onClick).also(container::addView)
        } else {
            null
        }

        editText.addTextChangedListener(object : SimpleTextWatcher {
            override fun afterTextChanged(s: Editable) {
                when {
                    s.isEmpty() && clearButton.parent != null -> {
                        TransitionManager.beginDelayedTransition(container)
                        container.removeView(clearButton)
                        actionButton?.also(container::addView)
                    }
                    s.isNotEmpty() && clearButton.parent == null -> {
                        TransitionManager.beginDelayedTransition(container)
                        container.addView(clearButton)
                        actionButton?.also(container::removeView)
                    }
                }
            }
        })
    }

    private fun isAllFieldsCorrect() =
        isCardNumberCorrect() && isExpiryCorrect() && cscEditText.length() >= MIN_LENGTH_CSC

    private fun createImageButtonForEdittext(
        container: ViewGroup,
        @IdRes actionViewId: Int,
        @DrawableRes icon: Int,
        action: () -> Unit
    ): View = LayoutInflater.from(container.context)
        .inflate(R.layout.ym_view_image_button_for_edittext, container, false).apply {
            (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    resources.getDimensionPixelOffset(R.dimen.ym_edittext_bottom_offset)
            id = actionViewId
            setOnClickListener {
                action()
            }
            (this as ImageView).setImageResource(icon)
        }
}
