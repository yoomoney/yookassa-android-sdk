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

package ru.yandex.money.android.sdk.impl.paymentAuth

import ru.yandex.money.android.sdk.BaseController
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractSuccessViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractViewModel
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthInputModel
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthOutputModel

internal class RequestPaymentAuthController(
        private val requestPaymentAuthUseCase: UseCase<RequestPaymentAuthInputModel, RequestPaymentAuthOutputModel>,
        private val progressPresenter: Presenter<RequestPaymentAuthProgressViewModel, ContractSuccessViewModel>,
        private val requestPaymentAuthPresenter: Presenter<RequestPaymentAuthOutputModel, ContractSuccessViewModel>,
        private val errorPresenter: Presenter<Exception, ContractFailViewModel>,
        resultConsumer: (ViewModel) -> Unit,
        logger: (String, Exception?) -> Unit
) : BaseController<RequestPaymentAuthInputModel, RequestPaymentAuthOutputModel, ContractViewModel>(
        resultConsumer = resultConsumer,
        logger = logger
) {

    override val progressViewModel: ContractSuccessViewModel
        get() = progressPresenter(RequestPaymentAuthProgressViewModel)

    override fun useCase(inputModel: RequestPaymentAuthInputModel) = requestPaymentAuthUseCase(inputModel)

    override fun presenter(outputModel: RequestPaymentAuthOutputModel) = requestPaymentAuthPresenter(outputModel)

    override fun exceptionPresenter(e: Exception) = errorPresenter(e)
}
