/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoo.sdk.kassa.payments.impl.contract

import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentOptionViewModel
import ru.yoo.sdk.kassa.payments.model.ViewModel

internal sealed class ContractViewModel : ViewModel()
internal object ContractProgressViewModel : ContractViewModel()
internal data class ContractSuccessViewModel(
    val shopTitle: CharSequence,
    val shopSubtitle: CharSequence,
    val paymentOption: PaymentOptionViewModel,
    val licenseAgreement: CharSequence,
    val showChangeButton: Boolean,
    val savePaymentMethodViewModel: SavePaymentMethodViewModel,
    val showAllowWalletLinking: Boolean,
    val paymentAuth: PaymentAuthViewModel?,
    val showPhoneInput: Boolean,
    val googlePayContractViewModel: GooglePayContractViewModel? = null
) : ContractViewModel()

internal data class ContractCompleteViewModel(
    val token: String,
    val type: PaymentMethodType
) : ContractViewModel()

internal data class GooglePayContractViewModel(
    val paymentOptionId: Int
) : ContractViewModel()

internal sealed class ContractFailViewModel : ContractViewModel()
internal data class ContractErrorViewModel(val error: CharSequence) : ContractFailViewModel()
internal class ContractRestartProcessViewModel : ContractFailViewModel()
internal object ContractUserAuthRequiredViewModel : ContractFailViewModel()
internal object ContractPaymentAuthRequiredViewModel : ContractFailViewModel()

internal sealed class PaymentAuthViewModel : ViewModel()
internal data class PaymentAuthStartViewModel(val amount: Amount) : PaymentAuthViewModel()
internal class PaymentAuthRestartViewModel : PaymentAuthViewModel()
internal class PaymentAuthShowUserAuthViewModel : PaymentAuthViewModel()
internal class PaymentAuthProgressViewModel : PaymentAuthViewModel()

internal object AuthNotRequiredViewModel : PaymentAuthViewModel()

internal sealed class PaymentAuthFormViewModel : PaymentAuthViewModel() {
    abstract val hint: CharSequence
    abstract val error: CharSequence?
}

internal data class PaymentAuthFormRetryViewModel(
    override val hint: CharSequence,
    override val error: CharSequence?,
    val timeout: Int?
) : PaymentAuthFormViewModel()

internal data class PaymentAuthFormNoRetryViewModel(
    override val hint: CharSequence,
    override val error: CharSequence?
) : PaymentAuthFormViewModel()

internal class PaymentAuthSuccessViewModel : PaymentAuthViewModel()
