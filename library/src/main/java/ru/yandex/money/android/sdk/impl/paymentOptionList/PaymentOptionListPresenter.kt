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

package ru.yandex.money.android.sdk.impl.paymentOptionList

import android.content.Context
import ru.yandex.money.android.sdk.ErrorPresenter
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.Wallet
import ru.yandex.money.android.sdk.impl.extensions.getAdditionalInfo
import ru.yandex.money.android.sdk.impl.extensions.getIcon
import ru.yandex.money.android.sdk.impl.extensions.getTitle
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListIsEmptyException

internal class PaymentOptionListPresenter(
    context: Context,
    private val showLogo: Boolean
) : Presenter<List<PaymentOption>, PaymentOptionListSuccessViewModel> {

    private val context = context.applicationContext

    override fun invoke(output: List<PaymentOption>) = constructOptionListSuccessViewModel(context, output, showLogo)
}

internal class ChangePaymentOptionPresenter(
    context: Context,
    private val showLogo: Boolean
) : Presenter<List<PaymentOption>, PaymentOptionListViewModel> {

    private val context = context.applicationContext

    override fun invoke(output: List<PaymentOption>): PaymentOptionListViewModel {
        if (output.size <= 1) {
            return PaymentOptionListCloseViewModel
        } else {
            return constructOptionListSuccessViewModel(context, output, showLogo)
        }
    }
}

private fun constructOptionListSuccessViewModel(context: Context, output: List<PaymentOption>, showLogo: Boolean) =
    PaymentOptionListSuccessViewModel(
        paymentOptions = output.map {
            PaymentOptionListItemViewModel(
                optionId = it.id,
                icon = it.getIcon(context),
                title = it.getTitle(context),
                additionalInfo = it.getAdditionalInfo(),
                canLogout = it is Wallet
            )
        },
        showLogo = showLogo
    )

internal class PaymentOptionListErrorPresenter(
    context: Context,
    private val showLogo: Boolean,
    private val errorPresenter: ErrorPresenter
) : Presenter<Exception, PaymentOptionListFailViewModel> {

    private val noPaymentOptionsError = context.applicationContext.getText(R.string.ym_no_payment_options_error)

    override fun invoke(e: Exception) = PaymentOptionListFailViewModel(
        error = noPaymentOptionsError.takeIf { e is PaymentOptionListIsEmptyException } ?: errorPresenter(e),
        showLogo = showLogo
    )
}

internal class PaymentOptionListProgressPresenter(
    showLogo: Boolean
) : Presenter<Unit, PaymentOptionListProgressViewModel> {

    private val progressViewModel = PaymentOptionListProgressViewModel(showLogo)

    override fun invoke(ignored: Unit) = progressViewModel
}
