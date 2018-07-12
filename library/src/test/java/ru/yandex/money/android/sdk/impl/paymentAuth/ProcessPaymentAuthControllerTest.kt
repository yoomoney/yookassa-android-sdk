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

package ru.yandex.money.android.sdk.impl.paymentAuth

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.SdkException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractErrorViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractSuccessViewModel
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthInputModel
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthOutputModel
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthSuccessOutputModel
import ru.yandex.money.android.sdk.stubContractSuccessViewModel
import ru.yandex.money.android.sdk.waitUntilEmpty
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
internal class ProcessPaymentAuthControllerTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val events = LinkedBlockingQueue<ViewModel>()

    @Mock
    private lateinit var mockUseCase: UseCase<ProcessPaymentAuthInputModel, ProcessPaymentAuthOutputModel>
    @Mock
    private lateinit var mockProgressPresenter: Presenter<ProcessPaymentAuthProgressViewModel, ContractSuccessViewModel>
    @Mock
    private lateinit var mockErrorPresenter: Presenter<Exception, ContractFailViewModel>
    @Mock
    private lateinit var mockSuccessPresenter: Presenter<ProcessPaymentAuthOutputModel, ContractSuccessViewModel>

    private lateinit var controller: ProcessPaymentAuthController

    @Before
    fun setUp() {
        on(mockProgressPresenter(ProcessPaymentAuthProgressViewModel)).thenReturn(stubContractSuccessViewModel())

        controller = ProcessPaymentAuthController(
                processPaymentUseCase = mockUseCase,
                progressPresenter = mockProgressPresenter,
                processPaymentAuthPresenter = mockSuccessPresenter,
                errorPresenter = mockErrorPresenter,
                resultConsumer = { events.put(it) },
                logger = { _, _ ->  }
        )
    }

    @Test
    fun successPass() {
        // prepare
        val testInputModel = ProcessPaymentAuthInputModel("passphrase", false)
        val output = ProcessPaymentAuthSuccessOutputModel()
        on(mockUseCase(testInputModel)).thenReturn(output)
        on(mockSuccessPresenter(output)).thenReturn(stubContractSuccessViewModel())

        // invoke
        controller(testInputModel)

        // assert
        waitUntilEmpty(events)
        inOrder(mockUseCase, mockProgressPresenter, mockSuccessPresenter, mockErrorPresenter).apply {
            verify(mockProgressPresenter).invoke(ProcessPaymentAuthProgressViewModel)
            verify(mockUseCase).invoke(testInputModel)
            verify(mockSuccessPresenter).invoke(output)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun failPass() {
        // prepare
        val exception = SdkException()
        val testInputModel = ProcessPaymentAuthInputModel("passphrase", false)
        on(mockUseCase(testInputModel)).then { throw exception }
        on(mockErrorPresenter(exception)).thenReturn(ContractErrorViewModel("err"))

        // invoke
        controller(testInputModel)

        // assert
        waitUntilEmpty(events)
        inOrder(mockUseCase, mockProgressPresenter, mockSuccessPresenter, mockErrorPresenter).apply {
            verify(mockProgressPresenter).invoke(ProcessPaymentAuthProgressViewModel)
            verify(mockUseCase).invoke(testInputModel)
            verify(mockErrorPresenter).invoke(exception)
            verifyNoMoreInteractions()
        }
    }
}
