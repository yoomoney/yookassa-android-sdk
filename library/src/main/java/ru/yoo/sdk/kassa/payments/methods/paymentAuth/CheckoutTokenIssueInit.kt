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

package ru.yoo.sdk.kassa.payments.methods.paymentAuth

import org.json.JSONObject
import ru.yoo.sdk.kassa.payments.Amount
import ru.yoo.sdk.kassa.payments.impl.extensions.toCheckoutTokenIssueInitResponse
import ru.yoo.sdk.kassa.payments.impl.extensions.toJsonObject
import ru.yoo.sdk.kassa.payments.model.ErrorCode
import ru.yoo.sdk.kassa.payments.model.ExtendedStatus

private const val CHECKOUT_TOKEN_ISSUE_INIT_PATH = "/checkout/token-issue-init"

private const val INSTANCE_NAME = "instanceName"
private const val SINGLE_AMOUNT_MAX = "singleAmountMax"
private const val PAYMENT_USAGE_LIMIT = "paymentUsageLimit"
private const val TMX_SESSION_ID = "tmxSessionId"
private const val USAGE_MULTIPLE = "Multiple"
private const val USAGE_SINGLE = "Single"

internal class CheckoutTokenIssueInitRequest(
    private val instanceName: String,
    private val singleAmountMax: Amount,
    private val multipleUsage: Boolean,
    private val tmxSessionId: String,
    userAuthToken: String,
    shopToken: String
) : CheckoutRequest<CheckoutTokenIssueInitResponse>(userAuthToken, shopToken) {

    override fun getUrl(): String = host + CHECKOUT_TOKEN_ISSUE_INIT_PATH

    override fun convertJsonToResponse(jsonObject: JSONObject) = jsonObject.toCheckoutTokenIssueInitResponse()

    override fun getPayload() = listOf(
        INSTANCE_NAME to instanceName,
        PAYMENT_USAGE_LIMIT to (USAGE_MULTIPLE.takeIf { multipleUsage } ?: USAGE_SINGLE),
        TMX_SESSION_ID to tmxSessionId)
        .let { it.takeIf { multipleUsage } ?: it.plus(SINGLE_AMOUNT_MAX to singleAmountMax.toJsonObject()) }
}

internal data class CheckoutTokenIssueInitResponse(
    val status: ExtendedStatus,
    val errorCode: ErrorCode?,
    val authContextId: String?,
    val processId: String?
)
