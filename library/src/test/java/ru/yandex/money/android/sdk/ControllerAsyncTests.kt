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

package ru.yandex.money.android.sdk

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.mockito.Mockito.mock
import ru.yandex.money.android.sdk.model.Controller
import ru.yandex.money.android.sdk.model.UseCase
import ru.yandex.money.android.sdk.model.ViewModel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ControllerAsyncTests {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val testValue = 1
    private val progressEvent = ViewModel()
    private val events: BlockingQueue<ViewModel> = LinkedBlockingQueue()
    private lateinit var executorService: ExecutorService
    private lateinit var mockUseCase: UseCase<Int, Int>
    private lateinit var testController: Controller<Int, Int, Any>

    @[Before Suppress("UNCHECKED_CAST")]
    fun setUp() {
        executorService = Executors.newSingleThreadExecutor()
        mockUseCase = mock(UseCase::class.java) as UseCase<Int, Int>
        on(mockUseCase.invoke(testValue)).thenReturn(testValue)
        testController = Controller(
            name = "TestController",
            useCase = mockUseCase,
            presenter = { it },
            errorPresenter = Exception::toString,
            progressPresenter = { progressEvent },
            logger = { _, _ -> },
            resultConsumer = { events.offer(it) }
        )
    }

    @After
    fun tearDown() {
        executorService.shutdown()
        executorService.awaitTermination(30, TimeUnit.SECONDS)
    }

    @Test
    fun `should not invoke useCase twice when started twice`() {
        // prepare

        // invoke
        testController(testValue)
        testController(testValue)

        // assert
        assertThat(events.take(), equalTo(progressEvent))
        assertThat(events.take(), equalTo(testValue as ViewModel))
        assertThat(events.size, equalTo(0))
    }

    @Test
    fun `should cancel last invoke when reset called`() {
        // prepare

        // invoke
        testController(testValue)
        testController.reset()
        testController(testValue)

        // assert
        assertThat(events.take(), equalTo(progressEvent))
        assertThat(events.take(), equalTo(progressEvent))
        assertThat(events.take(), equalTo(testValue as ViewModel))
        assertThat(events.size, equalTo(0))
    }
}
