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

@file:JvmName("TestData")

package ru.yandex.money.android.sdk

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.stubbing.OngoingStubbing
import ru.yandex.money.android.sdk.impl.contract.ContractSuccessViewModel
import ru.yandex.money.android.sdk.impl.contract.SavePaymentMethodViewModel
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.impl.payment.PaymentOptionViewModel
import ru.yandex.money.android.sdk.model.AbstractWallet
import ru.yandex.money.android.sdk.model.CardBrand
import ru.yandex.money.android.sdk.model.Fee
import ru.yandex.money.android.sdk.model.GooglePay
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.LinkedCardInfo
import ru.yandex.money.android.sdk.model.NewCard
import ru.yandex.money.android.sdk.model.NewCardInfo
import ru.yandex.money.android.sdk.model.PaymentOption
import ru.yandex.money.android.sdk.model.PaymentOptionInfo
import ru.yandex.money.android.sdk.model.SbolSmsInvoicing
import ru.yandex.money.android.sdk.model.Wallet
import java.math.BigDecimal
import java.util.Arrays
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

fun <T> on(arg: T): OngoingStubbing<T> = `when`(arg)

fun waitUntilEmpty(queue: BlockingQueue<Any>) {
    while (queue.poll(1, TimeUnit.SECONDS) != null);
}

internal fun createNewCardPaymentOption(id: Int): PaymentOption =
    NewCard(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = true
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
        paymentMethodType = PaymentMethodType.YANDEX_MONEY
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
        paymentMethodType = PaymentMethodType.YANDEX_MONEY
    )

internal fun createSbolSmsInvoicingPaymentOption(id: Int): PaymentOption =
    SbolSmsInvoicing(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = false
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
        paymentMethodType = PaymentMethodType.YANDEX_MONEY
    )

internal fun createGooglePayPaymentOptionWithFee(id: Int): PaymentOption =
    GooglePay(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = false
    )

internal fun createGooglePayPaymentOptionWithoutFee(id: Int): PaymentOption =
    GooglePay(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = null,
        savePaymentMethodAllowed = false
    )

internal fun createNewCardInfo(): PaymentOptionInfo =
    NewCardInfo(
        number = "1234567887654321",
        expirationMonth = "01",
        expirationYear = "2020",
        csc = "000"
    )

internal fun createLinkedCardInfo(): PaymentOptionInfo =
    LinkedCardInfo(csc = "000")

internal fun createAmount() = Amount(BigDecimal.TEN, RUB)

internal val savePaymentMethodMessage: (PaymentOption) -> CharSequence = { "message" }
internal val savePaymentMethodViewModelTurnOn = SavePaymentMethodViewModel.On("message")

internal fun stubContractSuccessViewModel() = ContractSuccessViewModel(
    shopTitle = "title",
    shopSubtitle = "subtitle",
    paymentOption = stubPaymentOptionViewModel(),
    licenseAgreement = "licenseAgreement",
    showChangeButton = false,
    showAllowWalletLinking = false,
    savePaymentMethodViewModel = savePaymentMethodViewModelTurnOn,
    paymentAuth = null,
    showPhoneInput = false
)

internal fun stubPaymentOptionViewModel() = PaymentOptionViewModel(
    optionId = 1,
    icon = mock(Drawable::class.java),
    name = "name",
    amount = "10,0 P"
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
