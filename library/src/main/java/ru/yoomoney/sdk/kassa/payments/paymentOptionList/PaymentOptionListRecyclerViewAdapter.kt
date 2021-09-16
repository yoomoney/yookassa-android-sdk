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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.ym_item_common.view.divider
import kotlinx.android.synthetic.main.ym_item_common.view.image
import kotlinx.android.synthetic.main.ym_item_common.view.options
import kotlinx.android.synthetic.main.ym_item_common.view.primaryText
import kotlinx.android.synthetic.main.ym_item_common.view.secondaryText
import kotlinx.android.synthetic.main.ym_item_payment_option.view.delete
import ru.yoomoney.sdk.kassa.payments.extensions.visible

internal class PaymentOptionListRecyclerViewAdapter internal constructor(
    private val paymentOptionClickListener: PaymentOptionClickListener,
    private val paymentOptionsListItem: List<PaymentOptionListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PaymentOptionsViewHolder(PaymentOptionView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val paymentOption = paymentOptionsListItem[position]
        val viewHolder = (holder as PaymentOptionsViewHolder)
        viewHolder.isSwipeAvailable = paymentOption.hasOptions && !paymentOption.instrumentId.isNullOrEmpty()

        viewHolder.view.apply {
            image.setImageDrawable(paymentOption.icon)
            primaryText.text = paymentOption.title

            with(secondaryText) {
                visible = paymentOption.additionalInfo != null
                text = paymentOption.additionalInfo
            }

            setOnClickListener {
                paymentOptionClickListener.onPaymentOptionClick(paymentOption.optionId, paymentOption.instrumentId)
            }

            divider.visible = position != itemCount - 1
            with(options) {
                visible = paymentOption.hasOptions
                setOnClickListener {
                    paymentOptionClickListener.onOptionsMenuClick(paymentOption.optionId, paymentOption.instrumentId)
                }
            }
            delete.setOnClickListener { paymentOptionClickListener.onDeleteClick(paymentOption.optionId, paymentOption.instrumentId) }
        }
    }

    override fun getItemCount() = paymentOptionsListItem.size

    interface PaymentOptionClickListener {
        fun onPaymentOptionClick(optionId: Int, instrumentId: String?)
        fun onOptionsMenuClick(optionId: Int, instrumentId: String?)
        fun onDeleteClick(optionId: Int, instrumentId: String?)
    }
}
