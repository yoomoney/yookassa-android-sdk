package ru.yoo.sdk.kassa.payments.impl

import ru.yoo.sdk.kassa.payments.ColorScheme

object InMemoryColorSchemeRepository {
    var colorScheme: ColorScheme = ColorScheme.getDefaultScheme()
}
