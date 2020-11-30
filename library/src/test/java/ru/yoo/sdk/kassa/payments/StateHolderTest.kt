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

package ru.yoo.sdk.kassa.payments

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import ru.yoo.sdk.kassa.payments.model.StateHolder
import ru.yoo.sdk.kassa.payments.model.ViewModel

class StateHolderTest {

    private val testEvent = TestViewModel()

    private lateinit var stateHolder: StateHolder
    private lateinit var listener1: TestStateListener
    private lateinit var listener2: TestStateListener

    @Before
    fun setUp() {
        stateHolder = StateHolder(uiExecutor = { it() })
        listener1 = mock(TestStateListener::class.java)
        listener2 = mock(TestStateListener::class.java)

        stateHolder += listener1
        stateHolder += listener2
    }

    @Test
    fun shouldNotifyAddedListeners() {
        // prepare

        // invoke
        stateHolder.onEvent(testEvent)

        // assert
        verify(listener1).invoke(testEvent)
        verify(listener2).invoke(testEvent)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun shouldNotNotifyRemovedListeners() {
        // prepare
        stateHolder -= listener1

        // invoke
        stateHolder.onEvent(testEvent)

        // assert
        verify(listener2).invoke(testEvent)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun shouldNotifyListenerIfAddedAfterEventHappens() {
        // prepare
        stateHolder -= listener1
        stateHolder.onEvent(testEvent)

        // invoke
        stateHolder += listener1

        // assert
        verify(listener1).invoke(testEvent)
        verify(listener2).invoke(testEvent)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun shouldNotNotifyListenersWithDifferentEventType() {
        // prepare
        val testEvent1 = TestViewModel1()
        val listener = mock(TestStateListener1::class.java)
        stateHolder += listener

        // invoke
        stateHolder.onEvent(testEvent1)

        // assert
        verify(listener).invoke(testEvent1)
        verifyNoMoreInteractions(listener, listener1, listener2)
    }

    @Test
    fun shouldNotifyListenersOfSubtypes() {
        // prepare
        val testEvent = TestViewModelSubtype()

        // invoke
        stateHolder.onEvent(testEvent)

        // assert
        verify(listener1).invoke(testEvent)
        verify(listener2).invoke(testEvent)
        verifyNoMoreInteractions(listener1, listener2)
    }

    private open class TestViewModel : ViewModel()
    private class TestViewModelSubtype : TestViewModel()
    private class TestViewModel1 : ViewModel()

    private interface TestStateListener : (TestViewModel) -> Unit
    private interface TestStateListener1 : (TestViewModel1) -> Unit
}