package ru.yoo.sdk.kassa.payments.ui

import android.content.Context
import android.util.AttributeSet
import ru.yoo.sdk.gui.widget.button.PrimaryButtonView
import ru.yoo.sdk.kassa.payments.R
import ru.yoo.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

class FlatButtonView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.ym_FlatButton_Style
) : PrimaryButtonView(context, attrs, defStyleAttr) {

    init {
        iconStateColor = InMemoryColorSchemeRepository.typeColorStateList(context)
        setTextColor(InMemoryColorSchemeRepository.typeColorStateList(context))
    }
}