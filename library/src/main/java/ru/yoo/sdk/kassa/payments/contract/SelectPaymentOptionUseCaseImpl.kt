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

import ru.yoo.sdk.kassa.payments.payment.PaymentOptionRepository
import ru.yoo.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoo.sdk.kassa.payments.model.YooMoney
import ru.yoo.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectedPaymentOptionOutputModel

internal class SelectPaymentOptionUseCaseImpl(
    private val getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
    private val paymentOptionRepository: PaymentOptionRepository
) : SelectPaymentOptionUseCase {

    override suspend fun select(): Contract.Action {
        val paymentOptionId = paymentOptionRepository.paymentOptionId ?: return Contract.Action.RestartProcess

        val paymentOptions = getLoadedPaymentOptionListRepository.getLoadedPaymentOptions()
        val option = paymentOptions.find { it.id == paymentOptionId } ?: return Contract.Action.LoadContractFailed(
            SelectedOptionNotFoundException(paymentOptionId)
        )

        val walletLinkingPossible = option is YooMoney && checkPaymentAuthRequiredGateway.checkPaymentAuthRequired()

        return Contract.Action.LoadContractSuccess(
            SelectedPaymentOptionOutputModel(
                paymentOption = option,
                walletLinkingPossible = walletLinkingPossible
            )
        )
    }
}