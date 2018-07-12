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
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class BaseControllerAsyncTests {

    private val testValue = 1
    private val progressEvent = ViewModel()
    private val events: BlockingQueue<ViewModel> = LinkedBlockingQueue()
    private lateinit var executorService: ExecutorService
    private lateinit var mockUseCase: UseCase<Int, Int>
    private lateinit var testController: BaseController<Int, Int, Any>

    @[Before Suppress("UNCHECKED_CAST")]
    fun setUp() {
        executorService = Executors.newSingleThreadExecutor()
        mockUseCase = mock(UseCase::class.java) as UseCase<Int, Int>
        on(mockUseCase.invoke(testValue)).thenReturn(testValue)
        testController = object : BaseController<Int, Int, Any>(
                logger = { _, _ ->  },
                resultConsumer = { events.offer(it) }
        ) {
            override val progressViewModel: ViewModel
                get() = this@BaseControllerAsyncTests.progressEvent

            override fun useCase(inputModel: Int): Int {
                return mockUseCase(inputModel)
            }

            override fun presenter(outputModel: Int) = outputModel

            override fun exceptionPresenter(e: Exception) = e.toString()
        }
    }

    @After
    fun tearDown() {
        executorService.shutdown()
        executorService.awaitTermination(30, TimeUnit.SECONDS)
    }

    @Test(timeout = 60000L)
    fun shouldNotInvokeUseCaseTwice_When_StartedTwice() {
        // prepare

        // invoke
        testController(testValue)
        testController(testValue)

        // assert
        assertThat(events.take(), equalTo(progressEvent))
        assertThat(events.take(), equalTo(testValue as ViewModel))
        assertThat(events.size, equalTo(0))
    }

    @Test(timeout = 60000L)
    fun shouldCancelLastInvoke_When_ResetCalled() {
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
