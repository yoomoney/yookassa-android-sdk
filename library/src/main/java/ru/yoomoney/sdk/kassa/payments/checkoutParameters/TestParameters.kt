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

package ru.yoomoney.sdk.kassa.payments.checkoutParameters

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.android.gms.wallet.WalletConstants
import kotlinx.android.parcel.Parcelize

/**
 * Wrapper for test parameters. This class is used in [Checkout.createTokenizeIntent].
 *
 * @param showLogs (optional) shows mSDK logs in the logcat (all mSDK logs start with tag "YooKassa.SDK"). Need to pass the true flag only in a test build. In the release build logging won't work.
 * @param googlePayTestEnvironment (optional) enables google pay test environment - all transactions made with
 * Google Pay will use [WalletConstants.ENVIRONMENT_TEST]. More at:
 * https://developers.google.com/pay/api/android/guides/test-and-deploy/integration-checklist#about-the-test-environment.
 * @param mockConfiguration (optional) configuration for mock parameters. If this parameter is present, mSDK will
 * work in offline test mode. Token created with this configuration can't be used for payments.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class TestParameters
@[JvmOverloads Keep] constructor(
    @Keep val showLogs: Boolean = false,
    @Keep val googlePayTestEnvironment: Boolean = false,
    @Keep val mockConfiguration: MockConfiguration? = null
) : Parcelable
