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

package ru.yandex.money.android.sdk.payment.tokenize

import ru.yandex.money.android.sdk.AbstractWallet
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.GooglePay
import ru.yandex.money.android.sdk.GooglePayInfo
import ru.yandex.money.android.sdk.LinkedCard
import ru.yandex.money.android.sdk.LinkedCardInfo
import ru.yandex.money.android.sdk.NewCard
import ru.yandex.money.android.sdk.NewCardInfo
import ru.yandex.money.android.sdk.PaymentOption
import ru.yandex.money.android.sdk.PaymentOptionInfo
import ru.yandex.money.android.sdk.SbolSmsInvoicing
import ru.yandex.money.android.sdk.SbolSmsInvoicingInfo
import ru.yandex.money.android.sdk.SelectedOptionNotFoundException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.WalletInfo
import ru.yandex.money.android.sdk.YandexMoney
import ru.yandex.money.android.sdk.payment.CheckPaymentAuthRequiredGateway
import ru.yandex.money.android.sdk.payment.GetLoadedPaymentOptionListGateway

private const val CANNOT_TOKENIZE_ABSTRACT_WALLET = "can not tokenize abstract wallet"

internal class TokenizeUseCase(
    private val getLoadedPaymentOptionListGateway: GetLoadedPaymentOptionListGateway,
    private val tokenizeGateway: TokenizeGateway,
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway
) : UseCase<TokenizeInputModel, TokenizeOutputModel> {

    override fun invoke(inputModel: TokenizeInputModel): TokenizeOutputModel {
        val option = getLoadedPaymentOptionListGateway
            .getLoadedPaymentOptions()
            .find { it.id == inputModel.paymentOptionId }
                ?: throw SelectedOptionNotFoundException(inputModel.paymentOptionId)

        check(option !is AbstractWallet) { CANNOT_TOKENIZE_ABSTRACT_WALLET }

        return when {
            isPaymentAuthRequired(option) -> TokenizePaymentAuthRequiredOutputModel(option.charge)
            isPaymentInfoRequired(option, inputModel) -> TokenizePaymentOptionInfoRequired(
                option = option,
                allowRecurringPayments = inputModel.allowRecurringPayments
            )
            else -> TokenOutputModel(
                token = tokenizeGateway.getToken(
                    paymentOption = option,
                    paymentOptionInfo = inputModel.paymentOptionInfo ?: WalletInfo(),
                    allowRecurringPayments = inputModel.allowRecurringPayments
                ),
                option = option
            )
        }
    }

    private fun isPaymentInfoRequired(option: PaymentOption, inputModel: TokenizeInputModel) =
        option is NewCard && inputModel.paymentOptionInfo !is NewCardInfo ||
                option is LinkedCard && inputModel.paymentOptionInfo !is LinkedCardInfo ||
                option is SbolSmsInvoicing && inputModel.paymentOptionInfo !is SbolSmsInvoicingInfo ||
                option is GooglePay && inputModel.paymentOptionInfo !is GooglePayInfo

    private fun isPaymentAuthRequired(option: PaymentOption) =
        option is YandexMoney && checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()
}

internal data class TokenizeInputModel(
    val paymentOptionId: Int,
    val allowRecurringPayments: Boolean,
    val paymentOptionInfo: PaymentOptionInfo? = null
)

internal sealed class TokenizeOutputModel

internal data class TokenOutputModel(
    val token: String,
    val option: PaymentOption
) : TokenizeOutputModel()

internal data class TokenizePaymentAuthRequiredOutputModel(
    val charge: Amount
) : TokenizeOutputModel()

internal data class TokenizePaymentOptionInfoRequired(
    val option: PaymentOption,
    val allowRecurringPayments: Boolean
) : TokenizeOutputModel()
