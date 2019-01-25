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

import android.content.Context
import android.support.annotation.StringRes
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.ApiMethodException
import ru.yandex.money.android.sdk.impl.contract.ContractErrorViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractPaymentAuthRequiredViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractUserAuthRequiredViewModel
import ru.yandex.money.android.sdk.model.ErrorCode
import ru.yandex.money.android.sdk.model.Presenter

internal class ProcessPaymentAuthErrorPresenter(
        context: Context,
        private val commonErrorPresenter: Presenter<Exception, CharSequence>
) : Presenter<Exception, ContractFailViewModel> {

    private val context = context.applicationContext
    private val allowedCodes = arrayOf(
            ErrorCode.INVALID_CONTEXT,
            ErrorCode.SESSION_DOES_NOT_EXIST,
            ErrorCode.SESSION_EXPIRED,
            ErrorCode.VERIFY_ATTEMPTS_EXCEEDED,
            ErrorCode.INVALID_ANSWER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.AUTH_EXPIRED,
            ErrorCode.TECHNICAL_ERROR,
            ErrorCode.UNKNOWN)

    override fun invoke(e: Exception): ContractFailViewModel {
        check(e is ApiMethodException && e.error.errorCode in allowedCodes || e !is ApiMethodException) { e }

        return when (e) {
            is ApiMethodException -> when (e.error.errorCode) {
                ErrorCode.INVALID_CONTEXT, ErrorCode.SESSION_DOES_NOT_EXIST,
                ErrorCode.SESSION_EXPIRED, ErrorCode.AUTH_EXPIRED -> ContractPaymentAuthRequiredViewModel
                ErrorCode.INVALID_TOKEN -> ContractUserAuthRequiredViewModel
                ErrorCode.VERIFY_ATTEMPTS_EXCEEDED ->
                    ContractErrorViewModel(context.getString(R.string.ym_verify_attempts_exceeded))
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

