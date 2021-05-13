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

package ru.yoomoney.sdk.kassa.payments.ui.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import ru.yoomoney.sdk.gui.utils.extensions.getDimensionPixelOffset
import ru.yoomoney.sdk.gui.utils.extensions.show
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoomoney.sdk.kassa.payments.extensions.clear
import ru.yoomoney.sdk.kassa.payments.extensions.configureForCardNumberInput
import ru.yoomoney.sdk.kassa.payments.extensions.findDefaultLocalActivityForIntent
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.visible
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardAnalyticsLogger
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardEvent
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardCvcInputError
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardExpiryInputError
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardNumberClearAction
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardNumberContinueAction
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardNumberInputError
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardNumberInputSuccess
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.CardNumberReturnToEdit
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.ScanBankCardAction
import ru.yoomoney.sdk.kassa.payments.paymentOptionInfo.isCorrectPan
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.utils.PatternInputFilter
import ru.yoomoney.sdk.kassa.payments.utils.SimpleTextWatcher
import ru.yoomoney.sdk.kassa.payments.utils.UNKNOWN_CARD_ICON
import ru.yoomoney.sdk.kassa.payments.utils.YearMonthTextWatcher
import ru.yoomoney.sdk.kassa.payments.utils.cursorAtTheEnd
import ru.yoomoney.sdk.kassa.payments.utils.getBankLogo
import ru.yoomoney.sdk.kassa.payments.utils.hide
import ru.yoomoney.sdk.kassa.payments.utils.isVisible
import ru.yoomoney.sdk.kassa.payments.utils.makeInvisible
import ru.yoomoney.sdk.kassa.payments.utils.setUpCursorColor
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private const val MIN_LENGTH_CSC = 3
private const val MIN_LENGTH_EXPIRY = 5
private const val MIN_LENGTH_CARD_NUMBER = 19


internal const val EXTRA_CARD_NUMBER = "cardNumber"
internal const val EXTRA_EXPIRY_MONTH = "expiryMonth"
internal const val EXTRA_EXPIRY_YEAR = "expiryYear"


internal class BankCardView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val cardView: MaterialCardView

    private val duration: Long

    private val bankCardConstraint: ConstraintLayout

    private val cardNumberEditText: EditText
    private val cardNumberEditDone: EditText
    private val expiryEditText: EditText
    private val cvcEditText: EditText

    private val cardNumberTitle: TextView
    private val expiryTitle: TextView
    private val cvcTitle: TextView

    private val detailsGroup: Group
    private val errorTextView: TextView
    private val bankCardLogo: ImageView
    private val cardScanView: ImageView
    private val continueWithCardView: ImageView
    private val clearCardNumberView: ImageView

    private val minExpiry = Calendar.getInstance()
    private val expiryFormat = SimpleDateFormat("MM/yy", Locale.US)

    private var onBankCardReadyListener: (NewCardInfo) -> Unit = {}
    private var onBankCardNotReadyListener: () -> Unit = {}
    private var onBankCardScanListener: (Intent) -> Unit = {}
    private var onPresetBankCardReadyListener: (String) -> Unit = {}
    private var bankCardAnalyticsLogger: BankCardAnalyticsLogger? = null

    private var expiryCorrectnessState: CorrectnessState = CorrectnessState.NA
        set(value) {
            takeIf { value == CorrectnessState.INCORRECT }
                ?.report(CardExpiryInputError)
            field = value
        }
    private var cardCorrectnessState: CorrectnessState = CorrectnessState.NA
        set(value) {
            when(value) {
                CorrectnessState.CORRECT -> CardNumberInputSuccess
                CorrectnessState.INCORRECT -> CardNumberInputError
                else -> null
            }?.apply(::report)

            field = value
        }
    private var cvcCorrectnessState: CorrectnessState = CorrectnessState.NA

    private var cardImageDrawable: Int = -1

    @ColorInt
    private val inActiveColor: Int

    @ColorInt
    private val activeColor: Int

    @ColorInt
    private val errorColor: Int

    private val primaryColor = InMemoryColorSchemeRepository.colorScheme.primaryColor

    private val isScanBankCardAvailable: Boolean

    private var mode = Mode.EDIT

    init {
        CheckoutInjector.inject(this)
        View.inflate(context, R.layout.ym_bank_card_view, this)

        duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        inActiveColor = getColor(R.color.color_type_ghost)
        activeColor = getColor(R.color.color_type_secondary)
        errorColor = getColor(R.color.color_type_alert)

        bankCardConstraint = findViewById(R.id.bankCardConstraint)

        cardView = findViewById(R.id.cardView)

        cardNumberEditText = findViewById(R.id.cardNumber)
        cardNumberEditDone = findViewById(R.id.cardNumberDone)
        expiryEditText = findViewById(R.id.expiry)
        cvcEditText = findViewById(R.id.cvc)

        cardNumberTitle = findViewById(R.id.cardNumberTitle)
        expiryTitle = findViewById(R.id.expiryTitle)
        cvcTitle = findViewById(R.id.cvcTitle)

        errorTextView = findViewById(R.id.error)
        detailsGroup = findViewById<Group>(R.id.detailsGroup).apply { hide() }
        bankCardLogo = findViewById(R.id.bankCard)
        cardScanView = findViewById(R.id.cardScan)

        val intent = Intent("ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD")

        isScanBankCardAvailable = context.findDefaultLocalActivityForIntent(intent)?.let {
            intent.component = ComponentName(it.packageName, it.name)
            cardScanView.apply {
                setColorFilter(primaryColor)
                setOnClickListener {
                    report(ScanBankCardAction)
                    onBankCardScanListener(intent)
                }
                show()
            }
            true
        } ?: false

        continueWithCardView = findViewById(R.id.continueWithCard)

        clearCardNumberView = findViewById(R.id.clear)
        clearCardNumberView.setOnClickListener {
            report(CardNumberClearAction)
            cardNumberEditText.clear()
        }

        listOf(cardScanView, continueWithCardView, clearCardNumberView).forEach {
            it.setColorFilter(primaryColor)
        }

        listOf(cardNumberEditText, cardNumberEditDone, expiryEditText, cvcEditText).forEach {
            it.setUpCursorColor(primaryColor)
        }

        setUpCardNumber()
        setUpExpiry()
        setUpCvc()
    }

    private fun setUpCardNumber() {
        with(cardNumberEditText) {
            val cardNumberInputDone = {
                fadeOut()

                with(cardNumberEditDone) {
                    fadeIn()
                    setText("•••• " + cardNumberEditText.text.toString().replace(" ", "").takeLast(4))
                }

                detailsGroup.show()

                listOf(
                    expiryTitle,
                    expiryEditText,
                    cvcTitle,
                    cvcEditText
                ).forEach { it.fadeIn() }

                cardNumberTitle.makeInActive()
                cardScanView.fadeOut()
                continueWithCardView.fadeOut()
                clearCardNumberView.fadeOut()

                setBankCardIcon(getBankLogo(text.toString()), true)

                if (expiryCorrectnessState == CorrectnessState.CORRECT) {
                    cvcEditText.requestFocus()
                } else {
                    expiryEditText.requestFocus()
                }
            }
            addTextChangedListener(
                AutoProceedWatcher(
                    textView = cardNumberTitle,
                    errorRes = R.string.ym_check_card_number_error,
                    minLength = MIN_LENGTH_CARD_NUMBER,
                    onDone = { cardNumberInputDone() },
                    afterTextChanged = {
                        val number = it.toString().filter(Char::isDigit)
                        when {
                            it.isEmpty() -> {
                                clearCardNumberView.fadeOut()
                                continueWithCardView.fadeOut()
                            }
                            number.length in 13..15 -> {
                                continueWithCardView.takeUnless { it.isVisible }?.fadeIn()
                                clearCardNumberView.fadeOut()
                            }
                            else -> {
                                continueWithCardView.fadeOut()
                                cardScanView.fadeOut()
                                clearCardNumberView.takeUnless { it.visible }?.fadeIn()
                            }
                        }
                    },
                    isCorrect = {
                        isCardNumberCorrect().also {
                            cardCorrectnessState = if (it) {
                                CorrectnessState.CORRECT
                            } else {
                                CorrectnessState.INCORRECT
                            }
                        }
                    }
                )
            )

            configureForCardNumberInput()
            filters += PatternInputFilter("[^\\d ]*")

            cardNumberEditDone.setOnFocusChangeListener { _, isFocused ->
                if (isFocused) {
                    detailsGroup.hide()

                    cardNumberEditDone.fadeOut()

                    cardNumberEditText.fadeIn()
                    cardNumberEditText.requestFocus()

                    continueWithCardView.fadeIn()

                    clearErrors(cardNumberTitle)

                    setBankCardIcon(getBankLogo(text.toString()))
                    report(CardNumberReturnToEdit)
                }
            }

            addTextChangedListener(object : SimpleTextWatcher {
                private var lastValue = ""

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    takeUnless { s.toString() == lastValue }?.let {
                        lastValue = s.toString()
                        cardScanView.takeIf { s.isEmpty() && isScanBankCardAvailable }?.fadeIn()

                        setBankCardIcon(getBankLogo(s.toString()))
                        checkBankCardReady()
                    }
                }
            })

            setOnEditorActionListener { _, actionId, event ->
                if ((actionId == EditorInfo.IME_ACTION_DONE
                            || event.action == KeyEvent.ACTION_DOWN
                            && event.keyCode == KeyEvent.KEYCODE_ENTER) && isCardNumberCorrect()
                ) {
                    cardNumberInputDone()
                }
                true
            }

            setOnFocusChangeListenerForTitleAndEditWithErrorPredicate(
                this,
                cardNumberTitle,
                R.string.ym_check_card_number_error
            ) { cardCorrectnessState == CorrectnessState.INCORRECT }

            continueWithCardView.setOnClickListener {
                report(CardNumberContinueAction)
                cardNumberInputDone()
            }
        }
    }

    private fun setBankCardIcon(@DrawableRes bankLogo: Int, shouldIgnoreUnknown: Boolean = false) {
        if (bankLogo != UNKNOWN_CARD_ICON || shouldIgnoreUnknown) {
            if (cardImageDrawable != bankLogo) {
                cardImageDrawable = bankLogo

                bankCardLogo.apply {
                    setImageDrawable(ContextCompat.getDrawable(context, bankLogo))
                    alpha = 0.0f;

                    animate()
                        .alpha(1.0f)
                        .translationX(getDimensionPixelOffset(R.dimen.ym_spaceXL).toFloat())
                        .duration = duration.takeIf { mode == Mode.EDIT } ?: 0
                }

                cardNumberEditText.animate()
                    .translationX(getDimensionPixelOffset(R.dimen.ym_spaceXL).toFloat())
                    .duration = duration;
            }

        } else {
            bankCardLogo.animate()
                .translationX(-100F)
                .alpha(0.0f)
                .duration = duration;

            cardNumberEditText.animate()
                .translationX(-1F)
                .duration = duration;

            cardImageDrawable = -1
        }
    }

    private fun setUpExpiry() {
        val year = minExpiry.get(Calendar.YEAR)
        val month = minExpiry.get(Calendar.MONTH)
        minExpiry.clear()
        minExpiry.set(year, month, 1)
        minExpiry.add(Calendar.DAY_OF_MONTH, -1)

        with(expiryEditText) {
            addTextChangedListener(
                AutoProceedWatcher(
                    textView = expiryTitle,
                    errorRes = R.string.ym_check_expiry_error,
                    minLength = MIN_LENGTH_EXPIRY,
                    onDone = { cvcEditText.requestFocus() },
                    isCorrect = {
                        isExpiryCorrect().also {
                            expiryCorrectnessState = if (it) {
                                CorrectnessState.CORRECT
                            } else {
                                CorrectnessState.INCORRECT
                            }
                        }
                    },
                    afterTextChanged = { expiryCorrectnessState = CorrectnessState.NA }
                )
            )
            addTextChangedListener(YearMonthTextWatcher())
            addTextChangedListener(object : SimpleTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (expiryCorrectnessState == CorrectnessState.NA) {
                        clearErrors(expiryTitle)
                    }
                    if (cvcCorrectnessState == CorrectnessState.INCORRECT) {
                        if (expiryCorrectnessState != CorrectnessState.INCORRECT) {
                            onError(cvcTitle, R.string.ym_check_cvc)
                        }
                    }
                    checkBankCardReady()
                }
            })

            setOnFocusChangeListenerForTitleAndEditWithErrorPredicate(
                this,
                expiryTitle,
                R.string.ym_check_expiry_error
            ) { expiryCorrectnessState == CorrectnessState.INCORRECT }
        }
    }

    private fun setUpCvc() {
        with(cvcEditText) {
            setOnEditorActionListener { _, actionId, event ->
                if ((actionId == EditorInfo.IME_ACTION_DONE
                            || event.action == KeyEvent.ACTION_DOWN
                            && event.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    if (length() < MIN_LENGTH_CSC) {
                        cvcCorrectnessState = CorrectnessState.INCORRECT
                        report(CardCvcInputError)
                        onError(cvcTitle, R.string.ym_check_cvc)
                    } else {
                        checkBankCardReady()
                    }
                }
                true
            }

            addTextChangedListener(
                AutoProceedWatcher(
                    textView = cvcTitle,
                    errorRes = R.string.ym_check_cvc,
                    minLength = MIN_LENGTH_CSC,
                    onDone = {}
                )
            )

            addTextChangedListener(object : SimpleTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    clearErrors(cvcTitle)

                    cvcCorrectnessState = if (s.length in 1..2) {
                        CorrectnessState.INCORRECT
                    } else {
                        CorrectnessState.CORRECT
                    }

                    if (expiryCorrectnessState == CorrectnessState.INCORRECT) {
                        onError(expiryTitle, R.string.ym_check_expiry_error)
                    }

                    checkBankCardReady()
                }
            })

            setOnFocusChangeListenerForTitleAndEditWithErrorPredicate(
                cvcEditText,
                cvcTitle,
                R.string.ym_check_cvc
            ) { cvcCorrectnessState == CorrectnessState.INCORRECT || (length() != 0 && length() < MIN_LENGTH_CSC) }
        }
    }

    private fun isCardNumberCorrect(): Boolean {
        return cardNumberEditText.text.toString().filter(Char::isDigit).isCorrectPan()
    }

    private fun isExpiryCorrect(): Boolean {
        return expiryEditText.length() == MIN_LENGTH_EXPIRY && try {
            expiryFormat.parse(expiryEditText.text.toString()) > minExpiry.time
        } catch (e: ParseException) {
            false
        }
    }

    private fun isAllFieldsCorrect() =
        isCardNumberCorrect() && isExpiryCorrect() && cvcEditText.length() >= MIN_LENGTH_CSC

    @ColorInt
    private fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    private fun checkBankCardReady() {
        if (isAllFieldsCorrect() && mode == Mode.EDIT) {
            val calendar = Calendar.getInstance()
            calendar.time = expiryFormat.parse(expiryEditText.text.toString())

            val card = NewCardInfo(
                number = cardNumberEditText.text.toString().filter(Char::isDigit),
                expirationMonth = String.format(Locale.getDefault(), "%tm", calendar),
                expirationYear = calendar.get(Calendar.YEAR).toString(),
                csc = cvcEditText.text.toString()
            )

            hideSoftKeyboard()
            cardView.requestFocus()
            onBankCardReadyListener(card)
        } else if (cvcEditText.length() >= MIN_LENGTH_CSC && mode == Mode.PRESET) {
            cardView.requestFocus()
            onPresetBankCardReadyListener(cvcEditText.text.toString())
        } else {
            onBankCardNotReadyListener()
        }
    }

    private fun TextView.makeActive() = setTextColor(activeColor)

    private fun TextView.makeInActive() = setTextColor(inActiveColor)

    fun setOnBankCardReadyListener(onBankCardReady: (NewCardInfo) -> Unit) {
        onBankCardReadyListener = onBankCardReady
    }

    fun setOnBankCardNotReadyListener(onBankCardNotReady: () -> Unit) {
        onBankCardNotReadyListener = onBankCardNotReady
    }

    fun setOnBankCardScanListener(listener: (Intent) -> Unit) {
        onBankCardScanListener = listener
    }

    fun setBankCardInfo(cardNumber: String?, expiryYear: Int?, expiryMonth: Int?) {
        cardNumber?.let(cardNumberEditText::setText)

        if (expiryMonth != null && expiryYear != null) {
            expiryEditText.setText(String.format("%02d%02d", expiryMonth, expiryYear))
        }

        if (!isCardNumberCorrect()) {
            cardNumberEditText.requestFocus()
        } else {
            cvcEditText.requestFocus()
        }
    }

    fun setOnPresetBankCardReadyListener(onPresetBankCardReady: (String) -> Unit) {
        onPresetBankCardReadyListener = onPresetBankCardReady
        checkBankCardReady()
    }

    fun presetBankCardInfo(cardNumber: String) {
        mode = Mode.PRESET

        cardNumberEditText.setText(cardNumber)
        cardNumberEditText.hide()
        cardScanView.hide()
        clearCardNumberView.hide()
        cardNumberTitle.makeInActive()
        bankCardLogo.show()

        with(cardNumberEditDone) {
            setText(cardNumber.replace("....".toRegex(), "$0 "))

            makeActive()

            with(ConstraintSet()) {
                clone(bankCardConstraint)
                constrainWidth(R.id.cardNumberDone, 0)
                applyTo(bankCardConstraint)
            }

            fadeIn()
        }


        cvcEditText.show()
        cvcTitle.show()

        expiryTitle.hide()
        expiryEditText.hide()

        cardNumberEditDone.isEnabled = false
    }

    fun setBankCardAnalyticsLogger(bankCardAnalyticsLogger: BankCardAnalyticsLogger) {
        this.bankCardAnalyticsLogger = bankCardAnalyticsLogger
    }

    private fun setOnFocusChangeListenerForTitleAndEditWithErrorPredicate(
        editText: EditText,
        title: TextView,
        @StringRes errorRes: Int,
        errorPredicate: () -> Boolean
    ) {
        editText.setOnFocusChangeListener { _, isFocused ->
            if (errorPredicate()) {
                onError(title, errorRes)
                editText.cursorAtTheEnd()
            } else {
                if (isFocused) {
                    editText.cursorAtTheEnd()
                    title.makeActive()
                } else {
                    title.makeInActive()
                }
            }
        }
    }

    private fun onError(textView: TextView, @StringRes errorRes: Int) {
        textView.setTextColor(errorColor)
        cardView.strokeColor = errorColor
        errorTextView.setText(errorRes)
        errorTextView.show()
    }

    private fun clearErrors(textView: TextView) {
        errorTextView.makeInvisible()
        textView.makeActive()
        cardView.strokeColor = getColor(R.color.ym_bank_card_stroke_color)
    }

    private fun View.fadeIn() {
        alpha = 0.0f
        show()
        animate()
            .alpha(1.0f)
            .duration = duration;
    }

    private fun View.fadeOut() {
        animate()
            .alpha(0.0f)
            .duration = duration;

        hide()
    }

    private fun report(event: BankCardEvent) {
        bankCardAnalyticsLogger?.onNewEvent(event)
    }

    private inner class AutoProceedWatcher internal constructor(
        private val textView: TextView,
        @StringRes private val errorRes: Int,
        @androidx.annotation.IntRange(from = 0)
        private val minLength: Int,
        private val onDone: () -> Unit,
        private val afterTextChanged: (String) -> Unit = {},
        private val isCorrect: (() -> Boolean)? = null
    ) : SimpleTextWatcher {

        private var lastValue = ""

        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            if (lastValue != s.toString()) {
                clearErrors(textView)
            } else {
                return
            }
            this.afterTextChanged(s.toString())
            lastValue = s.toString()
            if (s.length >= minLength) {
                if (isCorrect?.invoke() != false) {
                    onDone()
                } else {
                    onError(textView, errorRes)
                }
            }
        }
    }

    enum class CorrectnessState {
        NA,
        CORRECT,
        INCORRECT
    }

    enum class Mode {
        EDIT,
        PRESET
    }
}