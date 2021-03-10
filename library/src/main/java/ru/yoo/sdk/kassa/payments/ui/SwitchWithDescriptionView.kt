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
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.ym_switch_with_description_view.view.descriptionView
import kotlinx.android.synthetic.main.ym_switch_with_description_view.view.switchView
import ru.yoo.sdk.kassa.payments.R

open class SwitchWithDescriptionView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var title: CharSequence?
        set(value) {
            switchView.text = value
        }
        get() = switchView.text

    var description: CharSequence?
        set(value) {
            descriptionView.text = value
        }
        get() = descriptionView.text

    var isChecked: Boolean
        set(value) {
            switchView.isChecked = value
        }
        get() = switchView.isChecked

    var movementMethod: MovementMethod
        set(value) {
            switchView.movementMethod = value
        }
        get() = switchView.movementMethod

    init {
        inflate()
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ym_ListItemSwitchWithDescription, defStyleAttr, 0
        )

        attributes.getText(R.styleable.ym_ListItemSwitchWithDescription_ym_title)?.apply {
            title = this
        }

        attributes.getBoolean(R.styleable.ym_ListItemSwitchWithDescription_ym_isChecked, false).apply {
            isChecked = this
        }

        attributes.getText(R.styleable.ym_ListItemSwitchWithDescription_ym_description)?.apply {
            description = this
        }
    }

    private fun inflate() {
        View.inflate(context, R.layout.ym_switch_with_description_view, this)
    }
}

fun SwitchWithDescriptionView.onCheckedChangedListener(listener: ((Boolean) -> Unit)?) {
    listener?.let {
        switchView.setOnCheckedChangeListener { _, isChecked ->
            listener(isChecked)
        }
    } ?: switchView.setOnCheckedChangeListener(null)
}