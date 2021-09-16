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

package ru.yoomoney.sdk.kassa.payments.unbind

import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class UnbindBusinessLogic(
    private val showState: suspend (UnbindCard.State) -> UnbindCard.Action,
    private val showEffect: suspend (UnbindCard.Effect) -> Unit,
    private val source: suspend () -> UnbindCard.Action,
    private val unbindCardUseCase: UnbindCardUseCase
) : Logic<UnbindCard.State, UnbindCard.Action> {

    override fun invoke(
        state: UnbindCard.State,
        action: UnbindCard.Action
    ): Out<UnbindCard.State, UnbindCard.Action> = when (state) {
        is UnbindCard.State.Initial -> state.whenInitialization(action)
        is UnbindCard.State.ContentLinkedBankCard -> state.whenUnbinding(action)
        is UnbindCard.State.LoadingUnbinding -> state.whenLoadingUnbinding(action)
        else -> Out.skip(state, source)
    }

    private fun UnbindCard.State.Initial.whenInitialization(
        action: UnbindCard.Action
    ): Out<UnbindCard.State, UnbindCard.Action> {
        return when (action) {
            is UnbindCard.Action.StartDisplayData -> {
                when {
                    action.linkedCard != null -> {
                        Out(UnbindCard.State.ContentLinkedWallet(action.linkedCard)) {
                            input { showState(this.state) }
                        }
                    }
                    action.instrumentBankCard != null -> {
                        Out(UnbindCard.State.ContentLinkedBankCard(action.instrumentBankCard)) {
                            input { showState(this.state) }
                        }
                    }
                    else -> Out.skip(this, source)
                }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun UnbindCard.State.ContentLinkedBankCard.whenUnbinding(
        action: UnbindCard.Action
    ): Out<UnbindCard.State, UnbindCard.Action> {
        return when (action) {
            is UnbindCard.Action.StartUnbinding -> Out(UnbindCard.State.LoadingUnbinding(this.instrumentBankCard)) {
                input { showState(this.state) }
                input { unbindCardUseCase.unbindCard(state.instrumentBankCard.paymentInstrumentId) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun UnbindCard.State.LoadingUnbinding.whenLoadingUnbinding(
        action: UnbindCard.Action
    ): Out<UnbindCard.State, UnbindCard.Action> {
        return when (action) {
            is UnbindCard.Action.StartUnbinding -> Out(UnbindCard.State.LoadingUnbinding(this.instrumentBankCard)) {
                input { showState(this.state) }
                input { unbindCardUseCase.unbindCard(state.instrumentBankCard.paymentInstrumentId) }
            }
            is UnbindCard.Action.UnbindSuccess -> Out(this) {
                output { showEffect(UnbindCard.Effect.UnbindComplete(state.instrumentBankCard)) }
                input(source)
            }
            is UnbindCard.Action.UnbindFailed -> Out(this) {
                output { showEffect(UnbindCard.Effect.UnbindFailed(state.instrumentBankCard)) }
                input(source)
            }
            else -> Out.skip(this, source)
        }
    }
}
