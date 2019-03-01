package ru.yandex.money.android.sdk.impl.paymentAuth

import ru.yandex.money.android.sdk.model.ViewModel

internal sealed class SmsSessionRetryViewModel : ViewModel()
internal object SmsSessionRetryProgressViewModel : SmsSessionRetryViewModel()
