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

@file:JvmName("TestData")

package ru.yoomoney.sdk.kassa.payments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import java.math.BigDecimal
import java.util.Arrays

fun <T> on(arg: T): OngoingStubbing<T> = `when`(arg)

internal fun createNewCardPaymentOption(
    id: Int,
    savePaymentMethodAllowed: Boolean = true,
    createBinding: Boolean = false
): PaymentOption =
    BankCardPaymentOption(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = listOf(ConfirmationType.EXTERNAL),
        paymentInstruments = emptyList(),
        savePaymentInstrument = createBinding
    )

internal fun createBankCardPaymentOption(
    id: Int,
    instruments: List<PaymentInstrumentBankCard>,
    charge: Amount = Amount(BigDecimal.TEN, RUB),
    fee: Fee = Fee(
        Amount(BigDecimal.ONE, RUB),
        Amount(BigDecimal("0.5"), RUB)
    ),
    savePaymentMethodAllowed: Boolean = true,
    savePaymentInstrument: Boolean = false,
    confirmationTypes: List<ConfirmationType> = listOf(ConfirmationType.EXTERNAL)
): PaymentOption =
    BankCardPaymentOption(
        id = id,
        charge = charge,
        fee = fee,
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = confirmationTypes,
        paymentInstruments = instruments,
        savePaymentInstrument = savePaymentInstrument
    )

internal fun createWalletPaymentOption(id: Int): PaymentOption =
    Wallet(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        walletId = "12345654321",
        balance = Amount(BigDecimal.TEN, RUB),
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        savePaymentInstrument = false
    )

internal fun createAbstractWalletPaymentOption(id: Int): PaymentOption =
    AbstractWallet(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        savePaymentInstrument = false
    )

internal fun createSbolSmsInvoicingPaymentOption(id: Int, isSberPayAllowed: Boolean): PaymentOption =
    SberBank(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = false,
        confirmationTypes = if (isSberPayAllowed) {
            listOf(ConfirmationType.EXTERNAL, ConfirmationType.REDIRECT, ConfirmationType.MOBILE_APPLICATION)
        } else {
            listOf(ConfirmationType.EXTERNAL, ConfirmationType.REDIRECT)
        },
        savePaymentInstrument = false
    )

internal fun createLinkedCardPaymentOption(id: Int): PaymentOption =
    LinkedCard(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        cardId = "12345654321",
        brand = CardBrand.MASTER_CARD,
        pan = "1234567887654321",
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        savePaymentInstrument = false
    )

internal fun createGooglePayPaymentOptionWithFee(id: Int, savePaymentMethodAllowed: Boolean = false): PaymentOption =
    GooglePay(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = emptyList(),
        savePaymentInstrument = false
    )

internal fun createGooglePayPaymentOption(id: Int, fee: Fee? = null, savePaymentMethodAllowed: Boolean = false): PaymentOption =
    GooglePay(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = fee,
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = emptyList(),
        savePaymentInstrument = false
    )

internal fun equalToDrawable(drawable: Drawable?): Matcher<Drawable?> = object : BaseMatcher<Drawable?>() {
    override fun describeTo(description: Description?) {
        description?.appendText("drawables not matches ")
    }

    override fun matches(item: Any?): Boolean {
        if (drawable === item) return true
        return (item as? Drawable)?.pixelsEqualTo(drawable) == true
    }

}

internal fun createPaymentInstrumentBankCard(cscRequired: Boolean = false) = PaymentInstrumentBankCard(
    paymentInstrumentId = "paymentInstrumentId",
    last4 = "4321",
    first6 = "123456",
    cscRequired = cscRequired,
    cardType = CardBrand.MASTER_CARD
)

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun <T : Drawable, T1 : Drawable> T?.pixelsEqualTo(t: T1?) =
    this === null && t === null || this !== null && t !== null && this.toBitmap().pixelsEqualTo(t.toBitmap(), true)

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.pixelsEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
    if (width == other.width && height == other.height) {
        val res = Arrays.equals(toPixels(), other.toPixels())
        if (shouldRecycle) {
            doRecycle().also { otherBitmap.doRecycle() }
        }
        res
    } else false
} ?: kotlin.run { false }

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun <T : Drawable> T.toBitmap(): Bitmap {
    if (this is BitmapDrawable) return bitmap

    val drawable: Drawable = this
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.doRecycle() {
    if (!isRecycled) recycle()
}

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.toPixels() = IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }