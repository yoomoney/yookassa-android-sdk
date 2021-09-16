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

package ru.yoomoney.sdk.kassa.payments.extensions

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.model.RequestExecutionException
import ru.yoomoney.sdk.kassa.payments.model.ResponseParsingException
import ru.yoomoney.sdk.kassa.payments.model.ResponseReadingException
import ru.yoomoney.sdk.kassa.payments.methods.base.ApiRequest
import ru.yoomoney.sdk.kassa.payments.methods.base.DeleteRequest
import ru.yoomoney.sdk.kassa.payments.methods.base.MimeType
import ru.yoomoney.sdk.kassa.payments.methods.base.PostRequest
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.model.Result
import java.io.IOException

internal class CheckoutOkHttpClient(
    val okHttpClient: OkHttpClient,
    val errorReporter: ErrorLoggerReporter
)

internal fun <T: Any> CheckoutOkHttpClient.execute(apiRequest: ApiRequest<Result<T>>): Result<T> {
    val url = apiRequest.getUrl()
    val requestBuilder = Request.Builder()
        .url(url)
        .addHeader("accept", "application/json")

    apiRequest.getHeaders().forEach {
        requestBuilder.addHeader(it.first, it.second)
    }

    if (apiRequest is PostRequest<Result<T>>) {
        requestBuilder.addHeader("Content-Type", apiRequest.getMimeType().type)
        val body = createRequestBody(apiRequest.getMimeType(), apiRequest.getPayload())
        requestBuilder.post(RequestBody.create(null, body))
    } else if (apiRequest is DeleteRequest<Result<T>>) {
        val body = createRequestBody(apiRequest.getMimeType(), apiRequest.getPayload())
        requestBuilder.delete(RequestBody.create(null, body))
    } else {
        requestBuilder.get()
    }

    val request: Request = requestBuilder.build()

    val response: Response = try {
        okHttpClient.newCall(request).execute()
    } catch (e: IOException) {
        val exception = RequestExecutionException(request, e)
        errorReporter.report(exception)
        return Result.Fail(exception)
    }

    val stringBody: String = try {
        response.body!!.string()
    } catch (e: IOException) {
        val exception = ResponseReadingException(response, e)
        errorReporter.report(exception)
        return Result.Fail(exception)
    }

    val jsonObject = try {
        JSONObject(stringBody)
    } catch (e: JSONException) {
        val exception = ResponseParsingException(stringBody, e)
        errorReporter.report(exception)
        return Result.Fail(exception)
    }

    return try {
        apiRequest.convertJsonToResponse(jsonObject)
    } catch (e: JSONException) {
        val exception = ResponseParsingException(
            jsonObject.toString(
                4
            ), e
        )
        errorReporter.report(exception)
        Result.Fail(exception)
    }
}

internal fun createRequestBody(mimeType: MimeType, body: List<Pair<String, Any>>): String {
    return when (mimeType) {
        MimeType.JSON -> {
            val jsonObject = JSONObject()
            body.forEach { (key, value) ->
                jsonObject.put(key, value)
            }
            jsonObject.toString()
        }
        MimeType.X_WWW_FORM_URLENCODED -> body.joinToString(separator = "&") { (key, value) -> "$key=$value" }
    }
}
