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

package ru.yoo.sdk.kassa.payments.contract

import ru.yoo.sdk.kassa.payments.model.AbstractWallet
import ru.yoo.sdk.kassa.payments.model.Confirmation
import ru.yoo.sdk.kassa.payments.model.GooglePay
import ru.yoo.sdk.kassa.payments.model.GooglePayInfo
import ru.yoo.sdk.kassa.payments.model.LinkedCard
import ru.yoo.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoo.sdk.kassa.payments.model.NewCard
import ru.yoo.sdk.kassa.payments.model.NewCardInfo
import ru.yoo.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoo.sdk.kassa.payments.model.PaymentOption
import ru.yoo.sdk.kassa.payments.model.Result
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicing
import ru.yoo.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoo.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoo.sdk.kassa.payments.model.WalletInfo
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInfoRequired
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeRepository
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository

private const val CANNOT_TOKENIZE_ABSTRACT_WALLET = "can not tokenize abstract wallet"

internal class TokenizeUseCaseImpl(
    private val getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val tokenizeRepository: TokenizeRepository,
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
    private val paymenPaymentAuthTokenRepository: PaymentAuthTokenRepository,
    private val getConfirmation: (PaymentOption) -> Confirmation
) : TokenizeUseCase {

    override suspend fun tokenize(model: TokenizeInputModel): Contract.Action {
        val option = getLoadedPaymentOptionListRepository
            .getLoadedPaymentOptions()
            .find { it.id == model.paymentOptionId }
            ?: return Contract.Action.TokenizeFailed(SelectedOptionNotFoundException(model.paymentOptionId))

        check(option !is AbstractWallet) { CANNOT_TOKENIZE_ABSTRACT_WALLET }

        return when {
            isPaymentAuthRequired(option) -> Contract.Action.PaymentAuthRequired(option.charge)
            isPaymentInfoRequired(option, model) -> Contract.Action.TokenizeSuccess(TokenizePaymentOptionInfoRequired(
                option = option,
                savePaymentMethod = model.savePaymentMethod
            ))
            else -> {
                when(val result = tokenizeRepository.getToken(
                    paymentOption = option,
                    paymentOptionInfo = model.paymentOptionInfo ?: WalletInfo(),
                    confirmation = getConfirmation(option),
                    savePaymentMethod = model.savePaymentMethod
                )) {
                    is Result.Success -> Contract.Action.TokenizeSuccess(TokenOutputModel(token = result.value, option = option))
                    is Result.Fail -> Contract.Action.TokenizeFailed(result.value)
                }.also {
                    model.allowWalletLinking?.takeUnless { it }?.let {
                        paymenPaymentAuthTokenRepository.paymentAuthToken = null
                    }
                }
            }
        }
    }

    private fun isPaymentInfoRequired(option: PaymentOption, inputModel: TokenizeInputModel) =
        option is NewCard && inputModel.paymentOptionInfo !is NewCardInfo ||
                option is LinkedCard && inputModel.paymentOptionInfo !is LinkedCardInfo ||
                option is SbolSmsInvoicing && inputModel.paymentOptionInfo !is SbolSmsInvoicingInfo ||
                option is GooglePay && inputModel.paymentOptionInfo !is GooglePayInfo ||
                option is PaymentIdCscConfirmation && inputModel.paymentOptionInfo !is LinkedCardInfo


    private fun isPaymentAuthRequired(option: PaymentOption) =
        option is YooMoney && checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()
}