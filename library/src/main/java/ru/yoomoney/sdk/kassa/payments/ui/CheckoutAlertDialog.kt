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

package ru.yoomoney.sdk.kassa.payments.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.gui.widget.button.PrimaryButtonView
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

internal class CheckoutAlertDialog: YmAlertDialog() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        view.findViewById<PrimaryButtonView>(R.id.button_negative)
            ?.textColor = InMemoryColorSchemeRepository.colorScheme.primaryColor

        view.findViewById<PrimaryButtonView>(R.id.button_positive)
            ?.takeIf { arguments?.getBoolean(SHOULD_COLOR_POSITIVE_BUTTON) ?: false }
            ?.textColor = InMemoryColorSchemeRepository.colorScheme.primaryColor

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val windowParams = window.attributes
            windowParams.dimAmount = arguments?.getFloat(DIM_AMOUNT) ?: 0.3f
            windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            window.attributes = windowParams
        }
    }

    companion object {

        private const val TAG = "YmAlertDialog"

        private const val DIM_AMOUNT = "DIM_AMOUNT"
        private const val SHOULD_COLOR_POSITIVE_BUTTON = "SHOULD_COLOR_POSITIVE_BUTTON"

        @JvmStatic
        fun create(manager: FragmentManager, content: DialogContent, shouldColorPositiveColor: Boolean = false, dimAmount: Float? = null): CheckoutAlertDialog {
            return getIfShown(manager) ?: CheckoutAlertDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(CONTENT_KEY, content)
                    putBoolean(SHOULD_COLOR_POSITIVE_BUTTON, shouldColorPositiveColor)
                    dimAmount?.let { putFloat(DIM_AMOUNT, it) }
                }
            }
        }

        @JvmStatic
        fun getIfShown(manager: FragmentManager) = manager.findFragmentByTag(TAG) as CheckoutAlertDialog?
    }
}