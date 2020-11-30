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

package ru.yoo.sdk.kassa.payments.impl

import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import ru.yoo.sdk.kassa.payments.model.Error
import ru.yoo.sdk.kassa.payments.model.ErrorCode
import ru.yoo.sdk.kassa.payments.model.SdkException
import java.io.IOException

internal class NoInternetException : SdkException()

internal data class RequestExecutionException(val request: Request, val e: IOException) : SdkException(e)

internal data class ResponseReadingException(val response: Response, val e: IOException) : SdkException(e)

internal data class ResponseParsingException(val stringBody: String, val e: JSONException) : SdkException(e)

internal data class ApiMethodException(val error: Error) : SdkException() {
    constructor(errorCode: ErrorCode) : this(
        Error(
            errorCode
        )
    )
}

internal class PassphraseCheckFailedException : SdkException()