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

package ru.yandex.money.android.sdk.impl.metrics

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionInputModel
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionOutputModel

private typealias ChangePaymentOptionUseCase = UseCase<ChangePaymentOptionInputModel, ChangePaymentOptionOutputModel>

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ResetTokenizeSchemeWrapperTest {

    @Mock
    private lateinit var useCase: UseCase<Any, Any>
    @Mock
    private lateinit var tokenizeSchemeConsumer: TokenizeSchemeConsumer
    private lateinit var resetTokenizeSchemeWrapper: ResetTokenizeSchemeWrapper<Any, Any>

    @Before
    fun setUp() {
        resetTokenizeSchemeWrapper = ResetTokenizeSchemeWrapper(
            useCase = useCase,
            setTokenizeScheme = tokenizeSchemeConsumer
        )
    }

    @Test
    fun `should set null when UseCase invoked`() {
        // prepare

        // invoke
        resetTokenizeSchemeWrapper(Unit)

        // assert
        Mockito.inOrder(useCase, tokenizeSchemeConsumer).apply {
            verify(useCase).invoke(Unit)
            verify(tokenizeSchemeConsumer).invoke(null)
            verifyNoMoreInteractions()
        }
    }

    private interface TokenizeSchemeConsumer : (TokenizeScheme?) -> Unit
}
