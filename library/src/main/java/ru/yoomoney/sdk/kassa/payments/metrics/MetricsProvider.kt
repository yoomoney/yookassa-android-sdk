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

package ru.yoomoney.sdk.kassa.payments.metrics

import android.graphics.Color
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.ui.color.ColorScheme
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository

internal class SavePaymentMethodProvider : (PaymentParameters) -> SavePaymentMethodParam {

    override fun invoke(parameters: PaymentParameters) = when (parameters.savePaymentMethod) {
        SavePaymentMethod.OFF -> SavePaymentMethodOff()
        SavePaymentMethod.ON -> SavePaymentMethodOn()
        SavePaymentMethod.USER_SELECTS -> SavePaymentMethodUserSelect()
    }
}

internal class YooKassaIconProvider : (UiParameters) -> YookassaIcon {

    override fun invoke(
        uiParameters: UiParameters
    ) = if (uiParameters.showLogo) ShownYookassaIcon() else HiddenYookassaIcon()
}

internal class ColorMetricsProvider : (UiParameters) -> CustomColor {

    override fun invoke(
        uiParameters: UiParameters
    ) = if (uiParameters.colorScheme.primaryColor == ColorScheme.defaultPrimaryColor) {
        UsedDefaultColor()
    } else {
        UsedCustomColor()
    }
}

internal class UserAttiributionOnInitProvider(
    private val userAuthInfoRepository: UserAuthInfoRepository
) : (PaymentParameters) -> UserAttiributionOnInit {

    override fun invoke(
        paymentParameters: PaymentParameters
    ) = when {
        paymentParameters.customerId != null && userAuthInfoRepository.userAuthToken != null -> AllAttributesOnInit()
        paymentParameters.customerId != null -> CustomerIdOnInit()
        userAuthInfoRepository.userAuthToken != null -> YooMoneyOnInit()
        else -> NoneOnInit()
    }
}

