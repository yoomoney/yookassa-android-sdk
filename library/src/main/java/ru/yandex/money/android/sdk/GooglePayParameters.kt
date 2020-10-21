package ru.yandex.money.android.sdk

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import ru.yandex.money.android.sdk.GooglePayCardNetwork.AMEX
import ru.yandex.money.android.sdk.GooglePayCardNetwork.DISCOVER
import ru.yandex.money.android.sdk.GooglePayCardNetwork.JCB
import ru.yandex.money.android.sdk.GooglePayCardNetwork.MASTERCARD
import ru.yandex.money.android.sdk.GooglePayCardNetwork.VISA

/**
 * Settings for Google Pay payment method. This class is one of the parameters of [PaymentParameters].
 * @param allowedCardNetworks (optional) networks, that can be used by user. Google Pay will only show cards that belong
 * to this set. If no value is specified, the default set will be used.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class GooglePayParameters
@[JvmOverloads Keep] constructor(
    @Keep val allowedCardNetworks: Set<GooglePayCardNetwork> = setOf(AMEX, DISCOVER, JCB, VISA, MASTERCARD)
) : Parcelable

@Keep
enum class GooglePayCardNetwork {
    @Keep AMEX,
    @Keep DISCOVER,
    @Keep JCB,
    @Keep MASTERCARD,
    @Keep VISA,
    @Keep INTERAC,
    @Keep OTHER
}
