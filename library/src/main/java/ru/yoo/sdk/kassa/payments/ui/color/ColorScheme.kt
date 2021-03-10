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

package ru.yoo.sdk.kassa.payments.ui.color

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * This class can be used to customize mSDK color scheme. It is used in [UiParameters].
 *
 * @param primaryColor color, used by buttons, switches, editTexts etc.
 * Caution: don't use very bright colors - they won't be visible on a white background.
 * Caution: using red color will clash with error color on edit texts.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class ColorScheme
@Keep constructor(
    @[Keep ColorInt] val primaryColor: Int
) : Parcelable {

    @Keep
    companion object {
        /**
         * Current default color scheme, used by mSDK
         */
        @[JvmStatic Keep]
        fun getDefaultScheme(): ColorScheme {
            return ColorScheme(
                primaryColor = Color.rgb(
                    0,
                    168,
                    132
                )
            )
        }

        /**
         * Legacy color scheme, used by previous versions of mSDK
         */
        @[JvmStatic Keep]
        fun getLegacyScheme(): ColorScheme {
            return ColorScheme(
                primaryColor = Color.rgb(
                    255,
                    219,
                    77
                )
            )
        }
    }
}
