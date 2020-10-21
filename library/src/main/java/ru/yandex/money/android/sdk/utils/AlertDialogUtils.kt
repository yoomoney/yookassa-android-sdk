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

package ru.yandex.money.android.sdk.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.extensions.hideSoftKeyboard

internal fun showLogoutDialog(context: Context, accountName: CharSequence, view: View?) {
    AlertDialog.Builder(context, R.style.ym_DialogLogout).apply {
        setCancelable(true)
        setMessage(context.getString(R.string.ym_logout_dialog_message, accountName.trim()))
        setPositiveButton(context.getString(R.string.ym_logout_dialog_button_positive)) { _, _ ->
            view?.hideSoftKeyboard()
            AppModel.logoutController.invoke(Unit)
        }
        setNegativeButton(context.getString(R.string.ym_logout_dialog_button_negative)) { _, _ -> }
        show()
    }
}

internal fun showNoWalletDialog(context: Context) {
    AlertDialog.Builder(context, R.style.ym_DialogNoWallet).apply {
        setMessage(context.getString(R.string.ym_no_wallet_dialog_message))
        setPositiveButton(context.getString(R.string.ym_no_wallet_dialog_shoose_payment_option)) { _, _ ->
            AppModel.logoutController.invoke(Unit)
        }
        setCancelable(false)
        show()
    }
}
