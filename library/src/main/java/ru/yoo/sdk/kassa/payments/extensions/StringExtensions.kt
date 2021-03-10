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

@file:JvmName("StringExtensions")

package ru.yoo.sdk.kassa.payments.extensions

import android.util.Base64
import org.json.JSONObject
import ru.yoo.sdk.kassa.payments.model.ErrorCode

internal fun String.getJsonPayload(): JSONObject = split('.', limit = 3)[1]
    .also { require(it.isNotEmpty()) { this + " is not valid JWT string" } }
    .toByteArray(Charsets.UTF_8)
    .let { Base64.decode(it, Base64.DEFAULT) }
    .let { JSONObject(String(it, Charsets.UTF_8)) }

internal fun String.toErrorCode(): ErrorCode = when (this) {
    "invalid_request" -> ErrorCode.INVALID_REQUEST
    "not_supported" -> ErrorCode.NOT_SUPPORTED
    "invalid_credentials" -> ErrorCode.INVALID_CREDENTIALS
    "forbidden" -> ErrorCode.FORBIDDEN
    "internal_server_error" -> ErrorCode.INTERNAL_SERVER_ERROR
    "TechnicalError" -> ErrorCode.TECHNICAL_ERROR
    "InvalidScope" -> ErrorCode.INVALID_SCOPE
    "Forbidden" -> ErrorCode.FORBIDDEN
    "InvalidLogin" -> ErrorCode.INVALID_LOGIN
    "InvalidToken" -> ErrorCode.INVALID_TOKEN
    "InvalidSignature" -> ErrorCode.INVALID_SIGNATURE
    "SyntaxError" -> ErrorCode.SYNTAX_ERROR
    "IllegalParameters" -> ErrorCode.ILLEGAL_PARAMETERS
    "IllegalHeaders" -> ErrorCode.ILLEGAL_HEADERS
    "InvalidContext" -> ErrorCode.INVALID_CONTEXT
    "CreateTimeoutNotExpired" -> ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED
    "SessionsExceeded" -> ErrorCode.SESSIONS_EXCEEDED
    "UnsupportedAuthType" -> ErrorCode.UNSUPPORTED_AUTH_TYPE
    "InvalidAnswer" -> ErrorCode.INVALID_ANSWER
    "VerifyAttemptsExceeded" -> ErrorCode.VERIFY_ATTEMPTS_EXCEEDED
    "SessionDoesNotExist" -> ErrorCode.SESSION_DOES_NOT_EXIST
    "SessionExpired" -> ErrorCode.SESSION_EXPIRED
    "AccountNotFound" -> ErrorCode.ACCOUNT_NOT_FOUND
    "AuthRequired" -> ErrorCode.AUTH_REQUIRED
    "AuthExpired" -> ErrorCode.AUTH_EXPIRED
    else -> ErrorCode.UNKNOWN
}
