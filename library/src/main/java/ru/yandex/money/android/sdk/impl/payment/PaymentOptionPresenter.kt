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

package ru.yandex.money.android.sdk.impl.payment

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.impl.extensions.format
import ru.yandex.money.android.sdk.impl.extensions.getAdditionalInfo
import ru.yandex.money.android.sdk.impl.extensions.getIcon
import ru.yandex.money.android.sdk.impl.extensions.getTitle

internal class PaymentOptionPresenter(context: Context) : Presenter<PaymentOption, PaymentOptionViewModel> {

    private val context = context.applicationContext

    override fun invoke(paymentOption: PaymentOption) = PaymentOptionViewModel(
            optionId = paymentOption.id,
            icon = paymentOption.getIcon(context),
            name = paymentOption.getTitle(context),
            amount = paymentOption.charge.format().makeStartBold(),
            additionalInfo = paymentOption.getAdditionalInfo(),
            canLogout = paymentOption is Wallet
    )

    private fun CharSequence.makeStartBold() =
            (this as? Spannable ?: SpannableStringBuilder(this)).apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
}
