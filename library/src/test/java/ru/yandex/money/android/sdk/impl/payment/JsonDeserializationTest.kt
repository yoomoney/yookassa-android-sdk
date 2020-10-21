/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.sdk.impl.payment

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yandex.money.android.sdk.Amount
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.impl.extensions.RUB
import ru.yandex.money.android.sdk.impl.extensions.getStatus
import ru.yandex.money.android.sdk.impl.extensions.toAmount
import ru.yandex.money.android.sdk.impl.extensions.toAuthCheckResponse
import ru.yandex.money.android.sdk.impl.extensions.toAuthContextGetResponse
import ru.yandex.money.android.sdk.impl.extensions.toAuthSessionGenerateResponse
import ru.yandex.money.android.sdk.impl.extensions.toAuthTypeState
import ru.yandex.money.android.sdk.impl.extensions.toCheckoutTokenIssueExecuteResponse
import ru.yandex.money.android.sdk.impl.extensions.toCheckoutTokenIssueInitResponse
import ru.yandex.money.android.sdk.impl.extensions.toError
import ru.yandex.money.android.sdk.impl.extensions.getFee
import ru.yandex.money.android.sdk.impl.extensions.toPaymentMethodResponse
import ru.yandex.money.android.sdk.impl.extensions.toPaymentOptionResponse
import ru.yandex.money.android.sdk.impl.extensions.toTokenResponse
import ru.yandex.money.android.sdk.impl.extensions.toWalletCheckResponse
import ru.yandex.money.android.sdk.methods.PaymentMethodResponse
import ru.yandex.money.android.sdk.methods.PaymentOptionsResponse
import ru.yandex.money.android.sdk.methods.TokenResponse
import ru.yandex.money.android.sdk.methods.WalletCheckResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthCheckResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthContextGetResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutAuthSessionGenerateResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueExecuteResponse
import ru.yandex.money.android.sdk.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yandex.money.android.sdk.model.AbstractWallet
import ru.yandex.money.android.sdk.model.AuthType
import ru.yandex.money.android.sdk.model.AuthTypeState
import ru.yandex.money.android.sdk.model.CardBrand
import ru.yandex.money.android.sdk.model.CardInfo
import ru.yandex.money.android.sdk.model.Error
import ru.yandex.money.android.sdk.model.ErrorCode
import ru.yandex.money.android.sdk.model.ExtendedStatus
import ru.yandex.money.android.sdk.model.Fee
import ru.yandex.money.android.sdk.model.GooglePay
import ru.yandex.money.android.sdk.model.LinkedCard
import ru.yandex.money.android.sdk.model.NewCard
import ru.yandex.money.android.sdk.model.PaymentMethodBankCard
import ru.yandex.money.android.sdk.model.SbolSmsInvoicing
import ru.yandex.money.android.sdk.model.Status
import ru.yandex.money.android.sdk.model.Wallet
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class JsonDeserializationTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    @Test
    fun deserializeAmount() {
        val jsonObject = JSONObject(
            """{
             "value": "4.00",
             "currency": "RUB"
            }"""
        )
        assertThat(jsonObject.toAmount(), equalTo(Amount(BigDecimal("4.00"), RUB)))
    }

    @Test
    fun deserializeFeeWithServiceAndCounterparty() {
        val jsonObject = JSONObject().put(
            "fee", JSONObject(
                """{
            "service": {"value": "4.00","currency": "RUB"},
            "counterparty": {"value": "4.00","currency": "RUB"}
            }"""
            )
        )
        val amount = Amount(BigDecimal("4.00"), RUB)
        assertThat(jsonObject.getFee(), equalTo(Fee(amount, amount)))
    }

    @Test
    fun deserializeFeeWithCounterparty() {
        val jsonObject = JSONObject().put(
            "fee", JSONObject(
                """{
            "counterparty": {"value": "4.00","currency": "RUB"}
            }"""
            )
        )
        val amount = Amount(BigDecimal("4.00"), RUB)
        assertThat(jsonObject.getFee(), equalTo(Fee(null, amount)))
    }

    @Test
    fun deserializeFeeWithService() {
        val jsonObject = JSONObject().put(
            "fee", JSONObject(
                """{
            "service": {"value": "4.00","currency": "RUB"}
            }"""
            )
        )
        val amount = Amount(BigDecimal("4.00"), RUB)
        assertThat(jsonObject.getFee(), equalTo(Fee(amount, null)))
    }

    @Test
    fun deserializeError() {
        val jsonObject = JSONObject(
            """{
              "id": "ecf255db-cce8-4f15-8fc2-3d7a4678c867",
              "type": "error",
              "description": "Invalid API key provided",
              "parameter": "payment_method",
              "retry_after": 1800,
              "code": "invalid_request"
            }"""
        )

        val error = jsonObject.toError()

        assertThat(
            error, equalTo(
                Error(
                    ErrorCode.INVALID_REQUEST,
                    "ecf255db-cce8-4f15-8fc2-3d7a4678c867",
                    "Invalid API key provided",
                    "payment_method",
                    1800
                )
            )
        )
    }

    @Test
    fun deserializePaymentOptions() {
        val jsonObject = JSONObject(
            """{
          "type": "list",
          "items": [
            {
              "payment_method_type": "bank_card",
              "confirmation_types": [
                "redirect"
              ],
              "charge": {
                "value": "2.00",
                "currency": "RUB"
              },
              "save_payment_method": "allowed"
            },
            {
              "payment_method_type": "yandex_money",
              "confirmation_types": [
                "redirect"
              ],
              "charge": {
                "value": "3.00",
                "currency": "RUB"
              },
              "instrument_type": "wallet",
              "id": "123456789",
              "balance": {
                "value": "5.00",
                "currency": "RUB"
              },
              "save_payment_method": "allowed"
            },
            {
              "payment_method_type": "yandex_money",
              "confirmation_types": [
                "redirect"
              ],
              "charge": {
                "value": "4.00",
                "currency": "RUB"
              },
              "save_payment_method": "allowed"
            },
            {
              "payment_method_type": "yandex_money",
              "confirmation_types": [
                "redirect"
              ],
              "charge": {
                "value": "5.00",
                "currency": "RUB"
              },
              "id": "123456789",
              "instrument_type": "linked_bank_card",
              "card_name": "My card",
              "card_mask": "518901******0446",
              "card_type": "MasterCard",
              "save_payment_method": "allowed"
            },
            {
              "payment_method_type": "sberbank",
              "confirmation_types": [
                "redirect", "external"
              ],
              "charge": {
                "value": "2.00",
                "currency": "RUB"
              },
              "save_payment_method": "forbidden"
            },
            {
              "payment_method_type": "google_pay",
              "charge": {
                "value": "5.00",
                "currency": "RUB"
              },
              "save_payment_method": "forbidden"
            }
          ]
        }"""
        )

        val newCard = NewCard(0, Amount(BigDecimal("2.00"), RUB), null, true)
        val wallet = Wallet(
            1, Amount(BigDecimal("3.00"), RUB),
            null, "123456789", Amount(BigDecimal("5.00"), RUB), true, PaymentMethodType.YANDEX_MONEY
        )
        val abstractWallet = AbstractWallet(2, Amount(BigDecimal("4.00"), RUB), null, true,
            PaymentMethodType.YANDEX_MONEY
        )
        val bankCard = LinkedCard(
            3, Amount(BigDecimal("5.00"), RUB), null,
            "123456789", CardBrand.MASTER_CARD, "518901******0446", "My card", true, PaymentMethodType.YANDEX_MONEY
        )
        val sberbank = SbolSmsInvoicing(4, Amount(BigDecimal("2.00"), RUB), null, false)
        val googlePay = GooglePay(5, Amount(BigDecimal("5.00"), RUB), null, false)

        val paymentOptions =
            PaymentOptionsResponse(listOf(newCard, wallet, abstractWallet, bankCard, sberbank, googlePay), null)

        assertThat(jsonObject.toPaymentOptionResponse(), equalTo(paymentOptions))
    }

    @Test
    fun deserializePaymentMethod() {
        val jsonObject = JSONObject(
            """{
                "type": "bank_card",
                "id": "1da5c87d-0984-50e8-a7f3-8de646dd9ec9",
                "saved": true,
                "title": "Основная карта",
                "csc_required": true,
                "card": {
                    "first6": "427918",
                    "last4": "7918",
                    "expiry_year": "2017",
                    "expiry_month": "07",
                    "card_type": "MasterCard",
                    "source": "google_pay"
                }
            }"""
        )
        val paymentMethod = PaymentMethodResponse(
            paymentMethodBankCard = PaymentMethodBankCard(
                type = PaymentMethodType.BANK_CARD,
                id = "1da5c87d-0984-50e8-a7f3-8de646dd9ec9",
                saved =  true,
                cscRequired = true,
                title = "Основная карта",
                card = CardInfo(
                    first = "427918",
                    last = "7918",
                    expiryYear = "2017",
                    expiryMonth = "07",
                    cardType = CardBrand.MASTER_CARD,
                    source = PaymentMethodType.GOOGLE_PAY
                )
            ),
            error = null
        )

        assertThat(jsonObject.toPaymentMethodResponse(), equalTo(paymentMethod))
    }

    @Test
    fun deserializePaymentOptionsWithError() {
        val jsonObject = JSONObject(
            """{
            "id": "ecf255db-cce8-4f15-8fc2-3d7a4678c867",
            "type": "error",
            "description": "Invalid API key provided",
            "parameter": "payment_method",
            "retry_after": 1800,
            "code": "forbidden"
        }"""
        )
        val paymentOptions = PaymentOptionsResponse(
            listOf(),
            Error(
                ErrorCode.FORBIDDEN, "ecf255db-cce8-4f15-8fc2-3d7a4678c867",
                "Invalid API key provided",
                "payment_method",
                1800
            )
        )
        assertThat(jsonObject.toPaymentOptionResponse(), equalTo(paymentOptions))
    }

    @Test
    fun deserializeTokenResponse() {
        val jsonObject = JSONObject(
            """{
              "payment_token": "+u7PDjMTkf08NtD66P6+eYWa2yjU3gsSIhOOO+OWsOg=",
              "confirmative": false
            }"""
        )
        val tokenResponse = TokenResponse("+u7PDjMTkf08NtD66P6+eYWa2yjU3gsSIhOOO+OWsOg=", null)
        assertThat(jsonObject.toTokenResponse(), equalTo(tokenResponse))
    }

    @Test
    fun deserializeAuthContextGetResponseWithSuccessStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "error": "InvalidContext",
              "result": {
                "authTypes": [
                  {
                    "type": "Sms",
                    "attemptsCount": 10,
                    "attemptsLeft": 10,
                    "canBeIssued": false,
                    "codeLength": 6,
                    "codesLeft": 10,
                    "enabled": true,
                    "hasActiveSession": false,
                    "sessionsLeft": 10,
                    "isSessionRequired": true,
                    "sessionTimeLeft": 20,
                    "nextSessionTimeLeft": 30
                  }
                ],
                "defaultAuthType": "Sms"
              }
            }"""
        )
        val authContextGetResponse =
            CheckoutAuthContextGetResponse(
                Status.SUCCESS, null, arrayOf(
                    AuthTypeState(
                        AuthType.SMS,
                        30
                    )
                ), AuthType.SMS
            )
        assertThat(jsonObject.toAuthContextGetResponse(), equalTo(authContextGetResponse))
    }

    @Test
    fun deserializeAuthContextGetResponseWithSuccessStatusAndOneDisabledElementAndOneUnknownElement() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "error": "InvalidContext",
              "result": {
                "authTypes": [
                  {
                    "type": "Sms",
                    "attemptsCount": 10,
                    "attemptsLeft": 10,
                    "canBeIssued": false,
                    "codeLength": 6,
                    "codesLeft": 10,
                    "enabled": true,
                    "hasActiveSession": false,
                    "sessionsLeft": 10,
                    "isSessionRequired": true,
                    "sessionTimeLeft": 20,
                    "nextSessionTimeLeft": 30
                  },
                  {
                    "type": "Totp",
                    "attemptsCount": 10,
                    "attemptsLeft": 10,
                    "canBeIssued": false,
                    "codeLength": 6,
                    "codesLeft": 10,
                    "enabled": false,
                    "hasActiveSession": false,
                    "sessionsLeft": 10,
                    "isSessionRequired": true,
                    "sessionTimeLeft": 20,
                    "nextSessionTimeLeft": 30
                  }
                ],
                "defaultAuthType": "Sms"
              }
            }"""
        )
        val authContextGetResponse =
            CheckoutAuthContextGetResponse(
                Status.SUCCESS, null, arrayOf(
                    AuthTypeState(
                        AuthType.SMS,
                        30
                    )
                ), AuthType.SMS
            )
        assertThat(jsonObject.toAuthContextGetResponse(), equalTo(authContextGetResponse))
    }

    @Test
    fun deserializeAuthContextGetResponseWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "InvalidContext"
            }"""
        )
        val authContextGetResponse =
            CheckoutAuthContextGetResponse(Status.REFUSED, ErrorCode.INVALID_CONTEXT, emptyArray(), AuthType.UNKNOWN)
        assertThat(jsonObject.toAuthContextGetResponse(), equalTo(authContextGetResponse))
    }

    @Test
    fun deserializeAuthContextGetResponseWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "InvalidScope"
              }
            }"""
        )
        val authContextGetResponse =
            CheckoutAuthContextGetResponse(Status.UNKNOWN, ErrorCode.INVALID_SCOPE, emptyArray(), AuthType.UNKNOWN)
        assertThat(jsonObject.toAuthContextGetResponse(), equalTo(authContextGetResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueInitWithSuccessStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "result": {
                "authContextId": "12345",
                "processId": "6789"
              }
            }"""
        )
        val checkoutTokenIssueInitResponse =
            CheckoutTokenIssueInitResponse(ExtendedStatus.SUCCESS, null, "12345", "6789")
        assertThat(jsonObject.toCheckoutTokenIssueInitResponse(), equalTo(checkoutTokenIssueInitResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueInitWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "IllegalParameters"
            }"""
        )
        val checkoutTokenIssueInitResponse =
            CheckoutTokenIssueInitResponse(ExtendedStatus.REFUSED, ErrorCode.ILLEGAL_PARAMETERS, null, null)
        assertThat(jsonObject.toCheckoutTokenIssueInitResponse(), equalTo(checkoutTokenIssueInitResponse))
    }


    @Test
    fun deserializeCheckoutTokenIssueInitWithAuthRequiredStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "AuthRequired",
              "result": {
                "authContextId": "string",
                "processId": "string"
              }
            }"""
        )
        val checkoutTokenIssueInitResponse =
            CheckoutTokenIssueInitResponse(ExtendedStatus.AUTH_REQUIRED, null, "string", "string")
        assertThat(jsonObject.toCheckoutTokenIssueInitResponse(), equalTo(checkoutTokenIssueInitResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueInitWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "Forbidden"
              }
            }"""
        )
        val checkoutTokenIssueInitResponse =
            CheckoutTokenIssueInitResponse(ExtendedStatus.UNKNOWN, ErrorCode.FORBIDDEN, null, null)
        assertThat(jsonObject.toCheckoutTokenIssueInitResponse(), equalTo(checkoutTokenIssueInitResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueExecuteWithSuccessStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "result": {
                "accessToken": "token"
              }
            }"""
        )
        val checkoutTokenIssueExecuteResponse = CheckoutTokenIssueExecuteResponse(Status.SUCCESS, null, "token")
        assertThat(jsonObject.toCheckoutTokenIssueExecuteResponse(), equalTo(checkoutTokenIssueExecuteResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueExecuteWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "AuthRequired"
            }"""
        )
        val checkoutTokenIssueExecuteResponse =
            CheckoutTokenIssueExecuteResponse(Status.REFUSED, ErrorCode.AUTH_REQUIRED, null)
        assertThat(jsonObject.toCheckoutTokenIssueExecuteResponse(), equalTo(checkoutTokenIssueExecuteResponse))
    }

    @Test
    fun deserializeCheckoutTokenIssueExecuteWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "TechnicalError"
              }
            }"""
        )
        val checkoutTokenIssueExecuteResponse =
            CheckoutTokenIssueExecuteResponse(Status.UNKNOWN, ErrorCode.TECHNICAL_ERROR, null)
        assertThat(jsonObject.toCheckoutTokenIssueExecuteResponse(), equalTo(checkoutTokenIssueExecuteResponse))
    }

    @Test
    fun deserializeAuthTypeState() {
        val jsonObject = JSONObject(
            """{
                    "type": "Sms",
                    "attemptsCount": 10,
                    "attemptsLeft": 10,
                    "canBeIssued": false,
                    "codeLength": 6,
                    "codesLeft": 10,
                    "enabled": false,
                    "hasActiveSession": false,
                    "sessionsLeft": 10,
                    "isSessionRequired": true,
                    "sessionTimeLeft": 20,
                    "nextSessionTimeLeft": 30
                  }"""
        )
        val authTypeState =
            AuthTypeState(AuthType.SMS, 30)
        assertThat(jsonObject.toAuthTypeState(), equalTo(authTypeState))
    }

    @Test
    fun deserializeStatus() {
        val jsonObject = JSONObject(
            """{
                "status": "Refused"
              }"""
        )
        val status = Status.REFUSED
        assertThat(jsonObject.getStatus(), equalTo(status))
    }

    @Test
    fun deserializeAuthCheckWithSuccessStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "result": {
                "type": "Totp",
                "attemptsCount": 10,
                "attemptsLeft": 10,
                "canBeIssued": false,
                "codeLength": 6,
                "codesLeft": 10,
                "enabled": false,
                "hasActiveSession": false,
                "sessionsLeft": 10,
                "isSessionRequired": true,
                "sessionTimeLeft": 20,
                "nextSessionTimeLeft": 30
              }
            }"""
        )
        val authSessionGenerateResponse =
            CheckoutAuthCheckResponse(
                Status.SUCCESS, null,
                AuthTypeState(AuthType.TOTP, 30)
            )
        assertThat(jsonObject.toAuthCheckResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeAuthCheckWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "VerifyAttemptsExceeded"
            }"""
        )
        val authSessionGenerateResponse =
            CheckoutAuthCheckResponse(Status.REFUSED, ErrorCode.VERIFY_ATTEMPTS_EXCEEDED, null)
        assertThat(jsonObject.toAuthCheckResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeAuthCheckWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "InvalidToken"
              }
            }"""
        )
        val authSessionGenerateResponse = CheckoutAuthCheckResponse(Status.UNKNOWN, ErrorCode.INVALID_TOKEN, null)
        assertThat(jsonObject.toAuthCheckResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeAuthSessionGenerateWithSuccessStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Success",
              "error": "InvalidContext",
              "result": {
                "type": "Sms",
                "attemptsCount": 10,
                "attemptsLeft": 10,
                "canBeIssued": false,
                "codeLength": 6,
                "codesLeft": 10,
                "enabled": false,
                "hasActiveSession": false,
                "sessionsLeft": 10,
                "isSessionRequired": true,
                "sessionTimeLeft": 20,
                "nextSessionTimeLeft": 20
              }
            }"""
        )
        val authSessionGenerateResponse =
            CheckoutAuthSessionGenerateResponse(
                Status.SUCCESS, null,
                AuthTypeState(AuthType.SMS, 20)
            )
        assertThat(jsonObject.toAuthSessionGenerateResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeAuthSessionGenerateWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "CreateTimeoutNotExpired"
            }"""
        )
        val authSessionGenerateResponse =
            CheckoutAuthSessionGenerateResponse(Status.REFUSED, ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED, null)
        assertThat(jsonObject.toAuthSessionGenerateResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeAuthSessionGenerateWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "InvalidLogin"
              }
            }"""
        )
        val authSessionGenerateResponse =
            CheckoutAuthSessionGenerateResponse(Status.UNKNOWN, ErrorCode.INVALID_LOGIN, null)
        assertThat(jsonObject.toAuthSessionGenerateResponse(), equalTo(authSessionGenerateResponse))
    }

    @Test
    fun deserializeWalletCheckWithError() {
        val jsonObject = JSONObject(
            """{
              "error": {
                "type": "InvalidLogin"
              }
            }"""
        )
        val walletCheckResponse = WalletCheckResponse(Status.REFUSED, null)
        assertThat(jsonObject.toWalletCheckResponse(), equalTo(walletCheckResponse))
    }

    @Test
    fun deserializeWalletCheckWithWallet() {
        val jsonObject = JSONObject(
            """{
              "account_number":"410012312594138",
              "terms_and_conditions_apply_required":false,
              "status":"success"
            }""".trimMargin()
        )
        val walletCheckResponse = WalletCheckResponse(Status.SUCCESS, true)
        assertThat(jsonObject.toWalletCheckResponse(), equalTo(walletCheckResponse))
    }

    @Test
    fun deserializeWalletCheckWithoutWallet() {
        val jsonObject = JSONObject(
            """{
              "terms_and_conditions_apply_required":true,
              "status":"success"
            }"""
        )
        val walletCheckResponse = WalletCheckResponse(Status.SUCCESS, false)
        assertThat(jsonObject.toWalletCheckResponse(), equalTo(walletCheckResponse))
    }
}
