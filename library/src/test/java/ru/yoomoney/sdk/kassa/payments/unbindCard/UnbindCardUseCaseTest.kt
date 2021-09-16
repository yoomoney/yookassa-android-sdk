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
package ru.yoomoney.sdk.kassa.payments.unbindCard

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SuccessUnbinding
import ru.yoomoney.sdk.kassa.payments.payment.unbindCard.UnbindCardGateway
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCard
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardUseCaseImpl

class UnbindCardUseCaseTest {

    private val repository: UnbindCardGateway = mock()

    private val bindingId = "bindingId"
    private val successUnbinding = Result.Success(SuccessUnbinding)
    private val failedUnbinding = Result.Fail(ApiMethodException(ErrorCode.INVALID_REQUEST))

    private val useCase = UnbindCardUseCaseImpl(repository, mock())

    @Test
    fun `should return UnbindSuccess action for when success load`() {
        // given
        whenever(repository.unbindCard(bindingId)).thenReturn(successUnbinding)
        val expected = UnbindCard.Action.UnbindSuccess

        // when
        val actual = runBlocking { useCase.unbindCard(bindingId) }

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should return UnbindFailed action for when success load`() {
        // given
        whenever(repository.unbindCard(bindingId)).thenReturn(failedUnbinding)
        val expected = UnbindCard.Action.UnbindFailed

        // when
        val actual = runBlocking { useCase.unbindCard(bindingId) }

        // then
        assertThat(actual).isEqualTo(expected)
    }
}