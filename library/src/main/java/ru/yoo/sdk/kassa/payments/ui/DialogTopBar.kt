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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.backButton
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.logo
import kotlinx.android.synthetic.main.ym_dialog_top_bar.view.paymentTitle
import ru.yoo.sdk.gui.utils.extensions.tint
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository


class DialogTopBar
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var title: CharSequence?
        set(value) {
            paymentTitle.text = value
        }
        get() = paymentTitle.text

    var isLogoVisible: Boolean
        set(value) {
            logo.isVisible = value
        }
        get() = logo.isVisible

    init {
        this.inflate()
        val primaryColor = InMemoryColorSchemeRepository.colorScheme.primaryColor
        backButton.setImageDrawable(backButton.drawable.tint(primaryColor))

        val set = intArrayOf(android.R.attr.text)
        val a = context.obtainStyledAttributes(attrs, set)
        title = a.getText(0)
    }

    private fun inflate() {
        View.inflate(context, R.layout.ym_dialog_top_bar, this)
    }

    fun onBackButton(listener: ((View) -> Unit)?) {
        backButton.isVisible = listener != null
        backButton.setOnClickListener(listener)
    }
}