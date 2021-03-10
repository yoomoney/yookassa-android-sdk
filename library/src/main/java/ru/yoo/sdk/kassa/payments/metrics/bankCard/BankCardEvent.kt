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

package ru.yoo.sdk.kassa.payments.metrics.bankCard

sealed class BankCardEvent

object CardNumberInputSuccess: BankCardEvent() {
    override fun toString() = "cardNumberInputSuccess"
}

object CardCvcInputError: BankCardEvent() {
    override fun toString() = "cardCvcInputError"
}

object CardNumberContinueAction: BankCardEvent() {
    override fun toString() = "cardNumberContinueAction"
}

object CardNumberReturnToEdit: BankCardEvent() {
    override fun toString() = "cardNumberReturnToEdit"
}

object CardNumberClearAction: BankCardEvent() {
    override fun toString() = "cardNumberClearAction"
}

object ScanBankCardAction: BankCardEvent() {
    override fun toString() = "scanBankCardAction"
}

object CardNumberInputError: BankCardEvent() {
    override fun toString() = "cardNumberInputError"
}

object CardExpiryInputError: BankCardEvent() {
    override fun toString() = "cardExpiryInputError"
}