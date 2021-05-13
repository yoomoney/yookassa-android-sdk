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

package ru.yoomoney.sdk.kassa.payments.model

internal data class Error(
    val errorCode: ErrorCode,
    val id: String? = null,
    val description: String? = null,
    val parameter: String? = null,
    val retryAfter: Int? = null
)

internal enum class ErrorCode {
    INVALID_REQUEST,
    NOT_SUPPORTED,
    INVALID_CREDENTIALS,
    FORBIDDEN,
    INTERNAL_SERVER_ERROR,
    TECHNICAL_ERROR,
    INVALID_SCOPE,
    INVALID_LOGIN,
    INVALID_TOKEN,
    INVALID_SIGNATURE,
    SYNTAX_ERROR,
    ILLEGAL_PARAMETERS,
    ILLEGAL_HEADERS,
    INVALID_CONTEXT,
    CREATE_TIMEOUT_NOT_EXPIRED,
    SESSIONS_EXCEEDED,
    UNSUPPORTED_AUTH_TYPE,
    VERIFY_ATTEMPTS_EXCEEDED,
    INVALID_ANSWER,
    SESSION_DOES_NOT_EXIST,
    SESSION_EXPIRED,
    ACCOUNT_NOT_FOUND,
    AUTH_REQUIRED,
    AUTH_EXPIRED,
    UNKNOWN
}
