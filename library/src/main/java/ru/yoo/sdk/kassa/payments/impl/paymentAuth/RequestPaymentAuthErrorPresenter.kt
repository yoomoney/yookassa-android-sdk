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

package ru.yoo.sdk.kassa.payments.impl.paymentAuth

import android.content.Context
import androidx.annotation.StringRes
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.impl.ApiMethodException
import ru.yoo.sdk.kassa.payments.impl.contract.ContractErrorViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractFailViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractPaymentAuthRequiredViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractUserAuthRequiredViewModel
import ru.yoo.sdk.kassa.payments.model.ErrorCode
import ru.yoo.sdk.kassa.payments.model.Presenter

internal class RequestPaymentAuthErrorPresenter(
        context: Context,
        private val commonErrorPresenter: Presenter<Exception, CharSequence>
) : Presenter<Exception, ContractFailViewModel> {

    private val context = context.applicationContext

    private val allowedErrorCodes = arrayOf(
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INVALID_CONTEXT,
            ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED,
            ErrorCode.SESSIONS_EXCEEDED,
            ErrorCode.TECHNICAL_ERROR,
            ErrorCode.UNKNOWN)

    override fun invoke(e: Exception): ContractFailViewModel {
        check(e is ApiMethodException && e.error.errorCode in allowedErrorCodes || e !is ApiMethodException) { e }

        return when (e) {
            is ApiMethodException -> when (e.error.errorCode) {
                ErrorCode.INVALID_TOKEN -> ContractUserAuthRequiredViewModel
                ErrorCode.INVALID_CONTEXT, ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED, ErrorCode.SESSIONS_EXCEEDED ->
                    ContractPaymentAuthRequiredViewModel
                ErrorCode.TECHNICAL_ERROR -> fromResource(R.string.ym_server_error)
                ErrorCode.UNKNOWN -> fromResource(R.string.ym_unknown_error)
                else -> fromException(e)
            }
            else -> fromException(e)
        }
    }

    private fun fromResource(@StringRes resId: Int) = ContractErrorViewModel(context.getText(resId))

    private fun fromException(e: Exception) = ContractErrorViewModel(commonErrorPresenter(e))
}
