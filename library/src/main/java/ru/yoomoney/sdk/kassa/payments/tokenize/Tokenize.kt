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

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel

internal object Tokenize {

    sealed class State {
        object Start: State()

        data class Tokenize(val tokenizeInputModel: TokenizeInputModel): State()

        data class TokenizeError(
            val tokenizeInputModel: TokenizeInputModel,
            val error: Throwable
        ): State()
    }

    sealed class Action {
        data class Tokenize(val tokenizeInputModel: TokenizeInputModel): Action()
        data class TokenizeFailed(val error: Throwable) : Action()
        data class TokenizeSuccess(val content: TokenizeOutputModel): Action()
        data class PaymentAuthRequired(val charge: Amount): Action()
        object PaymentAuthSuccess: Action()
        object PaymentAuthCancel: Action()
    }

    sealed class Effect {
        data class TokenizeComplete(val tokenizeOutputModel: TokenizeOutputModel, val allowWalletLinking: Boolean) : Effect()
        data class PaymentAuthRequired(val charge: Amount, val allowWalletLinking: Boolean): Effect()
        object CancelTokenize: Effect()
    }
}