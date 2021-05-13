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

package ru.yoomoney.sdk.kassa.payments.utils

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import ru.yoomoney.sdk.kassa.payments.R
import java.lang.reflect.Field

internal fun EditText.setUpCursorColor(@ColorInt color: Int) {
    val shapeDrawable = ContextCompat.getDrawable(context, R.drawable.ym_edit_text_cursor)?.apply {
        DrawableCompat.setTint(
            DrawableCompat.wrap(this),
            color
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable = shapeDrawable
    } else {
        try {
            TextView::class.java.getDeclaredField("mCursorDrawableRes").apply {
                isAccessible = true
                val drawableResId: Int = getInt(this)

                val editorField: Field = TextView::class.java
                    .getDeclaredField("mEditor")
                editorField.isAccessible = true
                val editor: Any = editorField.get(this)

                val drawable: Drawable? = ContextCompat
                    .getDrawable(context, drawableResId)
                drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)

                editor.javaClass.getDeclaredField("mCursorDrawable").apply {
                    isAccessible = true
                    set(editor, arrayOf(drawable, drawable))
                }
            }
        } catch (e: Exception) {
            Log.d("EditText", "Failed to change cursor color")
        }
    }
}

internal fun EditText.cursorAtTheEnd() {
    setSelection(text.length);
}