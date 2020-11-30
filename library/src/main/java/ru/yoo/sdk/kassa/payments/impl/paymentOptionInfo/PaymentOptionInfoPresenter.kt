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

import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.ViewModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired

internal class PaymentOptionInfoPresenter(
    private val otherPresenter: Presenter<TokenizeOutputModel, ViewModel>
) : Presenter<TokenizeOutputModel, ViewModel> {
    override fun invoke(outputModel: TokenizeOutputModel) = when (outputModel) {
        is TokenizePaymentOptionInfoRequired -> when (outputModel.option) {
            is NewCard -> PaymentOptionInfoBankCardViewModel(
                optionId = outputModel.option.id,
                savePaymentOption = outputModel.savePaymentMethod
            )
            is LinkedCard -> PaymentOptionInfoLinkedCardViewModel(
                optionId = outputModel.option.id,
                savePaymentOption = outputModel.savePaymentMethod,
                title = R.string.ym_bank_card_title_edit,
                pan = outputModel.option.pan.chunked(4).joinToString(" ")
            )
            is PaymentIdCscConfirmation -> PaymentOptionInfoLinkedCardViewModel(
                optionId = outputModel.option.id,
                savePaymentOption = outputModel.savePaymentMethod,
                title = R.string.ym_bank_card_title_new,
                pan = (outputModel.option.first + "*****" + outputModel.option.last).chunked(4).joinToString(" ")
            )
            else -> throw IllegalArgumentException("${outputModel.option} is not allowed here")
        }
        else -> otherPresenter.invoke(outputModel)
    }
}

internal sealed class PaymentOptionInfoViewModel : ViewModel() {
    abstract val optionId: Int
    abstract val savePaymentOption: Boolean
}

internal data class PaymentOptionInfoBankCardViewModel(
    override val optionId: Int,
    override val savePaymentOption: Boolean
) : PaymentOptionInfoViewModel()

internal data class PaymentOptionInfoLinkedCardViewModel(
    override val optionId: Int,
    override val savePaymentOption: Boolean,
    val pan: CharSequence,
    val title: Int
) : PaymentOptionInfoViewModel()
