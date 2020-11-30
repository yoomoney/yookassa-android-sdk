/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoo.sdk.kassa.payments.impl.paymentOptionInfo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.ym_fragment_bank_card.*
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.impl.extensions.configureForCardNumberInput
import ru.yoo.sdk.kassa.payments.impl.extensions.findDefaultLocalActivityForIntent
import ru.yoo.sdk.kassa.payments.impl.extensions.showSoftKeyboard
import ru.yoo.sdk.kassa.payments.model.NewCardInfo
import ru.yoo.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoo.sdk.kassa.payments.utils.PatternInputFilter
import ru.yoo.sdk.kassa.payments.utils.SimpleTextWatcher
import ru.yoo.sdk.kassa.payments.utils.YearMonthTextWatcher
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal const val EXTRA_CARD_NUMBER = "cardNumber"
internal const val EXTRA_EXPIRY_MONTH = "expiryMonth"
internal const val EXTRA_EXPIRY_YEAR = "expiryYear"

private const val MIN_LENGTH_CARD_NUMBER = 19
private const val MIN_LENGTH_EXPIRY = 5
private const val REQUEST_CODE_SCAN_BANK_CARD = 0x37BD

internal class NewBankCardFragment : BankCardFragment() {

    private val intent = Intent("ru.yoo.sdk.kassa.payments.action.SCAN_BANK_CARD")
    private val expiryFormat = SimpleDateFormat("MM/yy", Locale.US)
    private val minExpiry = Calendar.getInstance()

    init {
        val year = minExpiry.get(Calendar.YEAR)
        val month = minExpiry.get(Calendar.MONTH)
        minExpiry.clear()
        minExpiry.set(year, month, 1)
        minExpiry.add(Calendar.DAY_OF_MONTH, -1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityInfo = view.context.findDefaultLocalActivityForIntent(intent)

        with(cardNumberEditText) {
            addTextChangedListener(
                AutoProceedWatcher(
                    inputLayout = cardNumberInputLayout,
                    minLength = MIN_LENGTH_CARD_NUMBER,
                    onDone = ::requestExpiryFieldFocus,
                    isCorrect = ::isCardNumberCorrect
                )
            )
            configureForCardNumberInput()
            filters += PatternInputFilter("[^\\d ]*")

            activityInfo?.also {
                intent.component = ComponentName(it.packageName, it.name)
                setupClearAndActionButton(cardNumberInputContainer, this, R.drawable.ym_ic_scan_card) {
                    startActivityForResult(intent, REQUEST_CODE_SCAN_BANK_CARD)
                }
            } ?: setupClearButton(cardNumberInputContainer, this)
        }

        with(expiryEditText) {
            addTextChangedListener(
                AutoProceedWatcher(
                    inputLayout = expiryInputLayout,
                    minLength = MIN_LENGTH_EXPIRY,
                    onDone = ::requestCscFieldFocus,
                    isCorrect = ::isExpiryCorrect
                )
            )
            addTextChangedListener(YearMonthTextWatcher())
            setupClearButton(expiryContainer, this)
        }

        setTitle(getString(R.string.ym_bank_card_title_new))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            cardNumberEditText?.apply {
                requestFocus()
                showSoftKeyboard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SCAN_BANK_CARD && resultCode == Activity.RESULT_OK) {
            data?.extras?.apply {
                getString(EXTRA_CARD_NUMBER)?.filter(Char::isDigit)?.also { cardNumberEditText.setText(it) }

                val expiryMonth = getInt(EXTRA_EXPIRY_MONTH).takeIf { it > 0 }
                val expiryYear = getInt(EXTRA_EXPIRY_YEAR).takeIf { it > 0 }?.rem(100)
                if (expiryMonth != null && expiryYear != null) {
                    expiryEditText.setText(String.format("%02d%02d", expiryMonth, expiryYear))
                }
            }
        }
    }

    override fun collectPaymentOptionInfo(): PaymentOptionInfo {
        val calendar = Calendar.getInstance()
        calendar.time = expiryFormat.parse(expiryEditText.text.toString())

        return NewCardInfo(
            number = cardNumberEditText.text.toString().filter(Char::isDigit),
            expirationMonth = String.format(Locale.getDefault(), "%tm", calendar),
            expirationYear = calendar.get(Calendar.YEAR).toString(),
            csc = cscEditText.text.toString()
        )
    }

    private fun requestExpiryFieldFocus() {
        expiryEditText.requestFocus()
    }

    private fun requestCscFieldFocus() {
        cscEditText.requestFocus()
    }

    override fun isCardNumberCorrect() = cardNumberEditText.text.toString().filter(Char::isDigit).isCorrectPan()

    override fun isExpiryCorrect(): Boolean {
        return expiryEditText.length() == MIN_LENGTH_EXPIRY &&
                try {
                    expiryFormat.parse(expiryEditText.text.toString()) > minExpiry.time
                } catch (e: ParseException) {
                    false
                }
    }

    private class AutoProceedWatcher internal constructor(
        private val inputLayout: TextInputLayout,
        @androidx.annotation.IntRange(from = 0)
        private val minLength: Int,
        private val onDone: () -> Unit,
        private val isCorrect: (() -> Boolean)? = null
    ) : SimpleTextWatcher {

        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            if (s.length >= minLength) {
                if (isCorrect?.invoke() != false) {
                    onDone()
                    inputLayout.error = null
                } else {
                    inputLayout.error = " "
                }
            } else if (s.isEmpty()) {
                inputLayout.error = null
            }
        }
    }
}
