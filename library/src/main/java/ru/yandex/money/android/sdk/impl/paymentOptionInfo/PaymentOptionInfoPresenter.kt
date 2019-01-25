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

import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.NewCard
import ru.yandex.money.android.sdk.model.Presenter
import ru.yandex.money.android.sdk.model.ViewModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeOutputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizePaymentOptionInfoRequired

internal class PaymentOptionInfoPresenter(
    private val otherPresenter: Presenter<TokenizeOutputModel, ViewModel>
) : Presenter<TokenizeOutputModel, ViewModel> {
    override fun invoke(outputModel: TokenizeOutputModel) = when (outputModel) {
        is TokenizePaymentOptionInfoRequired -> when (outputModel.option) {
            is NewCard -> PaymentOptionInfoBankCardViewModel(
                optionId = outputModel.option.id,
                allowRecurringPayments = outputModel.allowRecurringPayments
            )
            is LinkedCard -> PaymentOptionInfoLinkedCardViewModel(
                optionId = outputModel.option.id,
                allowRecurringPayments = outputModel.allowRecurringPayments,
                pan = outputModel.option.pan.chunked(4).joinToString(" ")
            )
            else -> throw IllegalArgumentException("${outputModel.option} is not allowed here")
        }
        else -> otherPresenter.invoke(outputModel)
    }
}

internal sealed class PaymentOptionInfoViewModel : ViewModel() {
    abstract val optionId: Int
    abstract val allowRecurringPayments: Boolean
}

internal data class PaymentOptionInfoBankCardViewModel(
    override val optionId: Int,
    override val allowRecurringPayments: Boolean
) : PaymentOptionInfoViewModel()

internal data class PaymentOptionInfoLinkedCardViewModel(
    override val optionId: Int,
    override val allowRecurringPayments: Boolean,
    val pan: CharSequence
) : PaymentOptionInfoViewModel()
