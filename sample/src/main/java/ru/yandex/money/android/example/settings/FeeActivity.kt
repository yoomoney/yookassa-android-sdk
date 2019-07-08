/*
 * The MIT License (MIT)
 * Copyright © 2019 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.example.settings

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import ru.yandex.money.android.example.R

class FeeActivity : AppCompatActivity() {

    private var fee = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fee)

        this.findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { v -> finish() }
        this.findViewById<TextView>(R.id.feeEditText).addTextChangedListener(FeeTextWatcher())

        fee = savedInstanceState?.getFloat(KEY_LAST_FEE) ?: Settings(this).serviceFee

        this.findViewById<TextView>(R.id.feeEditText).text = fee.toString()
        findViewById<View>(R.id.save).setOnClickListener { onSave() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat(KEY_LAST_FEE, fee)
    }

    private fun onSave() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putFloat(Settings.KEY_SERVICE_FEE, fee)
            .apply()
        finish()
    }

    private inner class FeeTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // does nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // does nothing
        }

        override fun afterTextChanged(s: Editable) {
            fee = try {
                findViewById<TextView>(R.id.feeEditText).text.toString().toFloat()
            } catch (e: NumberFormatException) {
                0f
            }

        }
    }

    companion object {
        private const val KEY_LAST_FEE = "lastFee"
    }
}
