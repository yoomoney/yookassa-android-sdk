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
import android.view.View
import kotlinx.android.synthetic.main.ym_fragment_bank_card.*
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.extensions.showSoftKeyboard
import ru.yandex.money.android.sdk.model.LinkedCardInfo
import kotlin.properties.Delegates

internal class EditBankCardFragment : BankCardFragment() {

    var pan by Delegates.observable<CharSequence?>(null) { _, _, _ ->
        showBankCardNumber()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(cardNumberEditText) {
            isEnabled = false
            showBankCardNumber()
        }

        with(expiryEditText) {
            isEnabled = false
            setText("**/**")
        }

        setTitle(getString(R.string.ym_bank_card_title_edit))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            cscEditText?.apply {
                requestFocus()
                showSoftKeyboard()
            }
        }
    }

    private fun showBankCardNumber() {
        cardNumberEditText?.setText(pan)
    }

    override fun isCardNumberCorrect() = true

    override fun isExpiryCorrect() = true

    override fun collectPaymentOptionInfo() =
        LinkedCardInfo(cscEditText.text.toString())
}
