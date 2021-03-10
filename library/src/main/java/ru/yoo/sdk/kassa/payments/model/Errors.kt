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

package ru.yoo.sdk.kassa.payments.model

import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import java.io.IOException

internal open class SdkException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}

internal data class SelectedOptionNotFoundException(val optionId: Int) : SdkException()

internal class UnhandledException(cause: Throwable?) : Exception(cause)

internal class NoInternetException : SdkException()

internal data class RequestExecutionException(val request: Request, val e: IOException) : SdkException(e)

internal data class ResponseReadingException(val response: Response, val e: IOException) : SdkException(e)

internal data class ResponseParsingException(val stringBody: String, val e: JSONException) : SdkException(e)

internal open class ApiMethodException(open val error: Error) : SdkException() {
    constructor(errorCode: ErrorCode) : this(Error(errorCode))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiMethodException

        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        return error.hashCode()
    }
}

internal data class AuthCheckApiMethodException(override val error: Error, val authState: AuthTypeState?) : ApiMethodException(error)

internal class PassphraseCheckFailedException : SdkException()