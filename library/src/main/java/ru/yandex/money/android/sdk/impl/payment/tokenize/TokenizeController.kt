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

package ru.yandex.money.android.sdk.impl.payment.tokenize

import ru.yandex.money.android.sdk.BaseController
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractProgressViewModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeInputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeOutputModel

internal class TokenizeController(
    private val tokenizeUseCase: UseCase<TokenizeInputModel, TokenizeOutputModel>,
    private val successPresenter: Presenter<TokenizeOutputModel, ViewModel>,
    private val errorPresenter: Presenter<Exception, ContractFailViewModel>,
    resultConsumer: (ViewModel) -> Unit,
    logger: (String, Exception?) -> Unit
) : BaseController<TokenizeInputModel, TokenizeOutputModel, ViewModel>(
    resultConsumer = resultConsumer,
    logger = logger
) {

    override val progressViewModel: ContractProgressViewModel
        get() = ContractProgressViewModel

    override fun useCase(inputModel: TokenizeInputModel) = tokenizeUseCase(inputModel)

    override fun presenter(outputModel: TokenizeOutputModel) = successPresenter(outputModel)

    override fun exceptionPresenter(e: Exception) = errorPresenter(e)
}
