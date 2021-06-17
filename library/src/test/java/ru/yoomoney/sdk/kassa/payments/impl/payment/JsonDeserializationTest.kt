/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.impl.payment

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.extensions.getFee
import ru.yoomoney.sdk.kassa.payments.extensions.getStatus
import ru.yoomoney.sdk.kassa.payments.extensions.toAmount
import ru.yoomoney.sdk.kassa.payments.extensions.toAuthCheckResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toAuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toAuthSessionGenerateResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toAuthTypeState
import ru.yoomoney.sdk.kassa.payments.extensions.toCheckoutTokenIssueExecuteResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toCheckoutTokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toError
import ru.yoomoney.sdk.kassa.payments.extensions.toPaymentMethodResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toPaymentOptionResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toTokenResponse
import ru.yoomoney.sdk.kassa.payments.extensions.toWalletCheckResponse
import ru.yoomoney.sdk.kassa.payments.methods.WalletCheckResponse
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutAuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.methods.paymentAuth.CheckoutTokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.CardInfo
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Error
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.GooglePay
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.NewCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Status
import ru.yoomoney.sdk.kassa.payments.model.Wallet
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
        assertThat(jsonObject.toAmount(), equalTo(
            Amount(
                BigDecimal("4.00"),
                RUB
            )
        ))
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
              "payment_method_type": "yoo_money",
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
              "payment_method_type": "yoo_money",
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
              "payment_method_type": "yoo_money",
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
                "redirect", "external", "mobile_application"
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

        val newCard = NewCard(
            0,
            Amount(BigDecimal("2.00"), RUB), null, true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        )
        val wallet = Wallet(
            1,
            Amount(BigDecimal("3.00"), RUB),
            null, "123456789",
            Amount(BigDecimal("5.00"), RUB), true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        )
        val abstractWallet = AbstractWallet(
            2,
            Amount(BigDecimal("4.00"), RUB), null, true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        )
        val bankCard = LinkedCard(
            3, Amount(BigDecimal("5.00"), RUB), null,
            "123456789", CardBrand.MASTER_CARD, "518901******0446", "My card", true, true,
            confirmationTypes = listOf(ConfirmationType.REDIRECT)
        )
        val sberbank = SberBank(
            4,
            Amount(BigDecimal("2.00"), RUB), null, false,
            listOf(ConfirmationType.REDIRECT, ConfirmationType.EXTERNAL, ConfirmationType.MOBILE_APPLICATION)
        )
        val googlePay = GooglePay(
            5,
            Amount(BigDecimal("5.00"), RUB), null, false, emptyList()
        )

        val paymentOptions: Result<List<PaymentOption>> = Result.Success(listOf(newCard, wallet, abstractWallet, bankCard, sberbank, googlePay))

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
        val paymentMethod: Result<PaymentMethodBankCard> = Result.Success(PaymentMethodBankCard(
            type = PaymentMethodType.BANK_CARD,
            id = "1da5c87d-0984-50e8-a7f3-8de646dd9ec9",
            saved = true,
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
        ))

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
        val paymentOptions: Result<List<PaymentOption>> = Result.Fail(
            ApiMethodException(
                Error(
                    ErrorCode.FORBIDDEN,
                    "ecf255db-cce8-4f15-8fc2-3d7a4678c867",
                    "Invalid API key provided",
                    "payment_method",
                    1800
                )
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
        val tokenResponse: Result<String> = Result.Success("+u7PDjMTkf08NtD66P6+eYWa2yjU3gsSIhOOO+OWsOg=")
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
        val authContextGetResponse: Result<CheckoutAuthContextGetResponse> = Result.Success(
            CheckoutAuthContextGetResponse(
                arrayOf(
                    AuthTypeState.SMS(
                        nextSessionTimeLeft = 30,
                        codeLength = 6,
                        attemptsCount = 10,
                        attemptsLeft = 10
                    )
                ),
                AuthType.SMS
            )
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
        val authContextGetResponse: Result<CheckoutAuthContextGetResponse> = Result.Success(
            CheckoutAuthContextGetResponse(
                arrayOf(
                    AuthTypeState.SMS(
                        nextSessionTimeLeft = 30,
                        codeLength = 6,
                        attemptsCount = 10,
                        attemptsLeft = 10
                    )
                ),
                AuthType.SMS
            )
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
        val authContextGetResponse: Result<CheckoutAuthContextGetResponse> = Result.Fail(
            ApiMethodException(
                ErrorCode.INVALID_CONTEXT
            )
        )
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
        val authContextGetResponse: Result<CheckoutAuthContextGetResponse> = Result.Fail(
            ApiMethodException(
                ErrorCode.INVALID_SCOPE
            )
        )
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
        val checkoutTokenIssueInitResponse: Result<CheckoutTokenIssueInitResponse> = Result.Success(
            CheckoutTokenIssueInitResponse.Success("6789")
        )
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
        val checkoutTokenIssueInitResponse: Result<CheckoutTokenIssueInitResponse> = Result.Fail(
            ApiMethodException(ErrorCode.ILLEGAL_PARAMETERS)
        )
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
        val checkoutTokenIssueInitResponse: Result<CheckoutTokenIssueInitResponse> =  Result.Success(CheckoutTokenIssueInitResponse.AuthRequired(
            authContextId = "string",
            processId = "string"
        ))
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
        val checkoutTokenIssueInitResponse: Result<CheckoutTokenIssueInitResponse> = Result.Fail(
            ApiMethodException(ErrorCode.FORBIDDEN)
        )
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
        val checkoutTokenIssueExecuteResponse: Result<String> = Result.Success("token")
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
        val checkoutTokenIssueExecuteResponse: Result<String> = Result.Fail(
            ApiMethodException(
                ErrorCode.AUTH_REQUIRED
            )
        )
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
        val checkoutTokenIssueExecuteResponse: Result<String> = Result.Fail(
            ApiMethodException(
                ErrorCode.TECHNICAL_ERROR
            )
        )
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
        val authTypeState: AuthTypeState =
            AuthTypeState.SMS(
                nextSessionTimeLeft = 30,
                codeLength = 6,
                attemptsCount = 10,
                attemptsLeft = 10
            )
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
    fun deserializeAuthCheckWithRefusedStatus() {
        val jsonObject = JSONObject(
            """{
              "status": "Refused",
              "error": "VerifyAttemptsExceeded"
            }"""
        )
        val authSessionGenerateResponse: Result<Unit> = Result.Fail(
            AuthCheckApiMethodException(
                Error(ErrorCode.VERIFY_ATTEMPTS_EXCEEDED),
                null
            )
        )
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
        val authSessionGenerateResponse: Result<Unit> = Result.Fail(
            ApiMethodException(
                ErrorCode.INVALID_TOKEN
            )
        )
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
        val authSessionGenerateResponse: Result<AuthTypeState> = Result.Success(
            AuthTypeState.SMS(
                nextSessionTimeLeft = 20,
                codeLength = 6,
                attemptsCount = 10,
                attemptsLeft = 10
            )
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
        val authSessionGenerateResponse: Result<AuthTypeState> = Result.Fail(
            ApiMethodException(
                ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED
            )
        )
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
        val authSessionGenerateResponse: Result<AuthTypeState> = Result.Fail(
            ApiMethodException(
                ErrorCode.INVALID_LOGIN
            )
        )
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
