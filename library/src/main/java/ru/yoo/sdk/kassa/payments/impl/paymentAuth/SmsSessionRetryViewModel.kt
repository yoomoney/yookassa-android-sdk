package ru.yoo.sdk.kassa.payments.impl.paymentAuth

import ru.yoo.sdk.kassa.payments.model.ViewModel

internal sealed class SmsSessionRetryViewModel : ViewModel()
internal object SmsSessionRetryProgressViewModel : SmsSessionRetryViewModel()
