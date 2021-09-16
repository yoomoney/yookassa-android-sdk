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

package ru.yoomoney.sdk.kassa.payments.tokenize

import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.NoConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoomoney.sdk.kassa.payments.model.WalletInfo
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInstrumentInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizePaymentOptionInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository

private const val CANNOT_TOKENIZE_ABSTRACT_WALLET = "can not tokenize abstract wallet"

internal class TokenizeUseCaseImpl(
    private val getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val tokenizeRepository: TokenizeRepository,
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
    private val paymenPaymentAuthTokenRepository: PaymentAuthTokenRepository
) : TokenizeUseCase {

    override suspend fun tokenize(model: TokenizeInputModel): Tokenize.Action {
        val option = getLoadedPaymentOptionListRepository
            .getLoadedPaymentOptions()
            .find { it.id == model.paymentOptionId }
            ?: return Tokenize.Action.TokenizeFailed(SelectedOptionNotFoundException(model.paymentOptionId))

        check(option !is AbstractWallet) { CANNOT_TOKENIZE_ABSTRACT_WALLET }

        return when {
            isPaymentAuthRequired(option) -> Tokenize.Action.PaymentAuthRequired(option.charge)
            else -> {
                val result = when (model) {
                    is TokenizePaymentOptionInputModel -> {
                        tokenizeRepository.getToken(
                            paymentOption = option,
                            paymentOptionInfo = model.paymentOptionInfo ?: WalletInfo(),
                            confirmation = model.confirmation,
                            savePaymentMethod = model.savePaymentMethod,
                            savePaymentInstrument = model.savePaymentInstrument
                        )
                    }
                    is TokenizeInstrumentInputModel -> {
                        tokenizeRepository.getToken(
                            instrumentBankCard = model.instrumentBankCard,
                            amount = option.charge,
                            savePaymentMethod = model.savePaymentMethod,
                            csc = model.csc,
                            confirmation = model.confirmation
                        )
                    }
                }

                when (result) {
                    is Result.Success -> Tokenize.Action.TokenizeSuccess(
                        TokenizeOutputModel(
                            token = result.value,
                            option = option,
                            instrumentBankCard = model.instrumentBankCard
                        )
                    )
                    is Result.Fail -> Tokenize.Action.TokenizeFailed(result.value)
                }.also {
                    model.allowWalletLinking.takeUnless { it }?.let {
                        paymenPaymentAuthTokenRepository.paymentAuthToken = null
                    }
                    model.allowWalletLinking?.let {
                        paymenPaymentAuthTokenRepository.isUserAccountRemember = model.allowWalletLinking
                    }
                }
            }
        }
    }


    private fun isPaymentAuthRequired(option: PaymentOption) =
        option is YooMoney && checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()
}