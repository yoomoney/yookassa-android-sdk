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

package ru.yandex.money.android.sdk.impl.contract

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.SdkException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionInputModel
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionOutputModel
import ru.yandex.money.android.sdk.waitUntilEmpty
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val TEST_ID = 1

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class SelectPaymentOptionControllerTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val states = LinkedBlockingQueue<ViewModel>()

    @Mock
    private lateinit var useCase: UseCase<SelectPaymentOptionInputModel, SelectPaymentOptionOutputModel>
    @Mock
    private lateinit var contractPresenter: Presenter<SelectPaymentOptionOutputModel, ContractViewModel>
    @Mock
    private lateinit var errorPresenter: Presenter<Exception, ContractFailViewModel>
    private lateinit var controller: SelectPaymentOptionController

    @Before
    fun setUp() {
        controller = SelectPaymentOptionController(
                selectPaymentOptionUseCase = useCase,
                contractPresenter = contractPresenter,
                errorPresenter = errorPresenter,
                resultConsumer = { states.add(it) },
                logger = { _, _ ->  }
        )
    }

    @Test
    fun successPass() {
        // prepare
        val output = mock(SelectPaymentOptionOutputModel::class.java)
        val contractViewModel = mock(ContractViewModel::class.java)
        on(useCase(TEST_ID)).thenReturn(output)
        on(contractPresenter(output)).thenReturn(contractViewModel)

        // invoke
        controller(TEST_ID)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, contractPresenter, errorPresenter).apply {
            verify(useCase).invoke(TEST_ID)
            verify(contractPresenter).invoke(output)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun failPass() {
        // prepare
        val exception = SdkException()
        val failViewModel = mock(ContractFailViewModel::class.java)
        on(useCase(TEST_ID)).then { throw exception }
        on(errorPresenter(exception)).thenReturn(failViewModel)

        // invoke
        controller(TEST_ID)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, contractPresenter, errorPresenter).apply {
            verify(useCase).invoke(TEST_ID)
            verify(errorPresenter).invoke(exception)
            verifyNoMoreInteractions()
        }
    }
}
