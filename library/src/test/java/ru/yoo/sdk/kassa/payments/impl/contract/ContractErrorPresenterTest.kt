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

package ru.yoo.sdk.kassa.payments.impl.contract

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import ru.yoo.sdk.kassa.payments.model.Presenter
import ru.yoo.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoo.sdk.kassa.payments.on

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ContractErrorPresenterTest {

    @Mock
    private lateinit var errorPresenter: Presenter<Exception, CharSequence>
    private lateinit var presenter: ContractErrorPresenter

    @Before
    fun setUp() {
        presenter = ContractErrorPresenter(errorPresenter)
    }

    @Test
    fun shouldNotInvokeErrorPresenterForKnownException() {
        // prepare
        val testException = SelectedOptionNotFoundException(1)

        // invoke
        presenter.invoke(testException)

        // assert
        verifyZeroInteractions(errorPresenter)
    }

    @Test
    fun shouldInvokeErrorPresenterForUnknownException() {
        // prepare
        val testException = Exception()
        on(errorPresenter.invoke(testException)).thenReturn("test")

        // invoke
        presenter.invoke(testException)

        // assert
        verify(errorPresenter).invoke(testException)
    }
}