package ru.yoomoney.sdk.kassa.payments.ui

import android.content.Context
import android.util.AttributeSet
import ru.yoomoney.sdk.gui.widget.button.PrimaryButtonView
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

internal class FlatButtonView
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