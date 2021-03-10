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

package ru.yoo.sdk.kassa.payments.metrics

private const val HASH_SEED = 31

internal sealed class Param {
    abstract val name: String
    abstract val value: String

    override fun toString() = "${javaClass.simpleName}[name=$name;value=$value]"

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is Param -> false
        name != other.name -> false
        value != other.value -> false
        else -> true
    }

    override fun hashCode() = HASH_SEED * name.hashCode() + value.hashCode()
}

internal sealed class TokenizeScheme : Param() {
    final override val name = "tokenizeScheme"
}

internal class TokenizeSchemeWallet : TokenizeScheme() {
    override val value = "wallet"
}

internal class TokenizeSchemeLinkedCard : TokenizeScheme() {
    override val value = "linked-card"
}

internal class TokenizeSchemeBankCard : TokenizeScheme() {
    override val value = "bank-card"
}

internal class TokenizeSchemeSbolSms : TokenizeScheme() {
    override val value = "sms-sbol"
}

internal class TokenizeSchemeGooglePay : TokenizeScheme() {
    override val value = "google-pay"
}

internal class TokenizeSchemeRecurring : TokenizeScheme() {
    override val value = "recurring-card"
}

internal sealed class AuthType : Param() {
    final override val name = "authType"
}

internal class AuthTypeWithoutAuth : AuthType() {
    override val value = "withoutAuth"
}

internal class AuthTypeYooMoneyLogin : AuthType() {
    override val value = "yooMoneyLogin"
}

internal class AuthTypePaymentAuth : AuthType() {
    override val value = "paymentAuth"
}

internal sealed class AuthTokenType : Param() {
    final override val name = "authTokenType"
}

internal class AuthTokenTypeSingle : AuthTokenType() {
    override val value = "single"
}

internal class AuthTokenTypeMultiple : AuthTokenType() {
    override val value = "multiple"
}

internal sealed class AuthYooMoneyLoginStatus : Param() {
    final override val name = "authLoginStatus"
}

internal class AuthYooMoneyLoginStatusSuccess : AuthYooMoneyLoginStatus() {
    override val value = "Success"
}

internal class AuthYooMoneyLoginStatusFail : AuthYooMoneyLoginStatus() {
    override val value = "Fail"
}

internal class AuthYooMoneyLoginStatusCanceled : AuthYooMoneyLoginStatus() {
    override val value = "Canceled"
}

internal class AuthYooMoneyLoginStatusWithoutWallet : AuthYooMoneyLoginStatus() {
    override val value = "WithoutWallet"
}

internal sealed class AuthPaymentStatus : Param() {
    final override val name = "authPaymentStatus"
}

internal class AuthPaymentStatusSuccess : AuthPaymentStatus() {
    override val value = "Success"
}

internal class AuthPaymentStatusFail : AuthPaymentStatus() {
    override val value = "Fail"
}
