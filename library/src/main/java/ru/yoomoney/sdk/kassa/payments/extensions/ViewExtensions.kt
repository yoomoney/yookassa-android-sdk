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

@file:JvmName("ViewExtensions")

package ru.yoomoney.sdk.kassa.payments.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ru.yoomoney.sdk.kassa.payments.R

internal var View.visible
    get() = visibility == View.VISIBLE
    set(show) {
        visibility = if (show) View.VISIBLE else View.GONE
    }

internal fun View.showSoftKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

internal fun View.hideSoftKeyboard() {
    (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.also { imm ->
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}

internal fun View.showSnackbar(
    message: CharSequence,
    @ColorRes textColorResId: Int,
    @ColorRes backgroundColorResId: Int
): Snackbar {
    return Snackbar.make(this, message, Snackbar.LENGTH_SHORT).apply {
        view.elevation = resources.getDimension(R.dimen.ym_elevationL)
        val textView =
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(context, textColorResId))
        view.setBackgroundColor(ContextCompat.getColor(view.context, backgroundColorResId))
        show()
    }
}
