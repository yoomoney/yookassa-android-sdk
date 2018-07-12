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
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class BaseControllerTests {

    private val progressViewModel = Any()
    private val states = mutableListOf<ViewModel>()
    private lateinit var mockUseCase: UseCase<Int, Int>
    private lateinit var testController: BaseController<Int, Int, ViewModel>

    @[Before Suppress("UNCHECKED_CAST")]
    fun setUp() {
        mockUseCase = mock(UseCase::class.java) as UseCase<Int, Int>
        testController = object : BaseController<Int, Int, ViewModel>(
                logger = { _, _ ->  },
                resultConsumer = { states.add(it) },
                bgExecutor = { it() }
        ) {
            override val progressViewModel: ViewModel
                get() = this@BaseControllerTests.progressViewModel

            override fun useCase(inputModel: Int) = mockUseCase(inputModel)

            override fun presenter(outputModel: Int) = outputModel

            override fun exceptionPresenter(e: Exception) = e.toString()

        }
    }

    @Test
    fun shouldReInvoke_UseCase_When_Retry() {
        // prepare
        val testValue = 1
        on(mockUseCase.invoke(testValue)).thenReturn(testValue)

        // invoke
        testController.invoke(testValue)
        testController.retry()

        // assert
        verify(mockUseCase, times(2)).invoke(testValue)
        verifyNoMoreInteractions(mockUseCase)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldFail_Retry_If_CalledBeforeInvoke() {
        // invoke
        testController.retry()

        // assert that fail with exception IllegalStateException
    }

    @Test
    fun shouldSend_ProgressEvent_Then_ResultEvent_When_Invoked() {
        // prepare
        val testValue = 1
        on(mockUseCase.invoke(testValue)).thenReturn(testValue)

        // invoke
        testController(testValue)

        // assert
        assertThat(states.size, equalTo(2))
        assertThat(states[0], equalTo(progressViewModel))
        assertThat(states[1], equalTo(testValue as ViewModel))
    }

    @Test
    fun shouldSend_ProgressEvent_Then_ErrorEvent() {
        // prepare
        val testException = SdkException("test")
        on(mockUseCase.invoke(anyInt())).then { throw testException }

        // invoke
        testController(1)

        // assert
        assertThat(states.size, equalTo(2))
        assertThat(states[0], equalTo(progressViewModel))
        assertThat(states[1], equalTo(testException.toString() as ViewModel))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldResetState_When_ResetCalled() {
        // prepare
        val testValue = 1
        on(mockUseCase.invoke(testValue)).thenReturn(testValue)
        testController(testValue)

        // invoke
        testController.reset()
        testController.retry()

        // assert that fail with exception IllegalStateException
    }
}
