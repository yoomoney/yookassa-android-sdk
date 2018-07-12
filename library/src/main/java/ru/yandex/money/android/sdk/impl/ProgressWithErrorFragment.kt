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

package ru.yandex.money.android.sdk.impl

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ym_fragment_loading_with_error.*
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.extensions.showChild

internal class ProgressWithErrorFragment : Fragment() {

    private var showProgressWhenViewCreated = false
    private var showErrorWhenViewCreated: CharSequence? = null
    private var errorActionListener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.ym_fragment_loading_with_error, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when {
            showProgressWhenViewCreated -> {
                animator.showChild(progressView)
                showProgressWhenViewCreated = false
            }
            showErrorWhenViewCreated != null -> {
                errorView.setErrorText(showErrorWhenViewCreated!!)
                animator.showChild(errorView)
                showErrorWhenViewCreated = null
            }
        }

        errorActionListener?.also {
            errorView.setErrorButtonListener(View.OnClickListener { it() })
        }
    }

    fun showProgress() {
        when (animator) {
            null -> showProgressWhenViewCreated = true
            else -> animator.showChild(progressView)
        }
    }

    fun showError(text: CharSequence) {
        when (animator) {
            null -> showErrorWhenViewCreated = text
            else -> {
                errorView.setErrorText(text)
                animator.showChild(errorView)
            }
        }
    }

    fun setOnErrorActionListener(listener: () -> Unit) {
        when (errorView) {
            null -> errorActionListener = listener
            else -> errorView.setErrorButtonListener(View.OnClickListener { listener() })
        }
    }
}
