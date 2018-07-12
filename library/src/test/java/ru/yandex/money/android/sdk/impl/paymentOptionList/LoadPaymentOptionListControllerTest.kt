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

package ru.yandex.money.android.sdk.impl.paymentOptionList

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import ru.yandex.money.android.sdk.SdkException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.createAmount
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListInputModel
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListOutputModel
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class LoadPaymentOptionListControllerTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val resultConsumer = LinkedBlockingQueue<ViewModel>()

    private val stubProgress = PaymentOptionListProgressViewModel(true)
    private val stubSuccess = PaymentOptionListSuccessViewModel(listOf(), true)
    private val stubFail = PaymentOptionListFailViewModel("", true)
    private val stubAmount = createAmount()

    @Mock
    private lateinit var useCase: UseCase<PaymentOptionListInputModel, PaymentOptionListOutputModel>
    private lateinit var controller: LoadPaymentOptionListController

    @Before
    fun setUp() {
        controller = LoadPaymentOptionListController(
                paymentOptionListUseCase = useCase,
                paymentOptionListPresenter = { stubSuccess },
                progressPresenter = { stubProgress },
                errorPresenter = { stubFail },
                resultConsumer = resultConsumer::put,
                logger = { _, _ ->  }
        )
    }

    @Test
    fun successPass() {
        // prepare
        on(useCase.invoke(stubAmount)).thenReturn(listOf())

        // invoke
        controller(stubAmount)

        // assert
        assertThat(resultConsumer.take(), instanceOf(PaymentOptionListProgressViewModel::class.java))
        assertThat(resultConsumer.take(), instanceOf(PaymentOptionListSuccessViewModel::class.java))
        assertThat(resultConsumer.poll(), nullValue())
    }

    @Test
    fun failPass() {
        // prepare
        on(useCase.invoke(stubAmount)).then { throw SdkException() }

        // invoke
        controller(stubAmount)

        // assert
        assertThat(resultConsumer.take(), instanceOf(PaymentOptionListProgressViewModel::class.java))
        assertThat(resultConsumer.take(), instanceOf(PaymentOptionListFailViewModel::class.java))
        assertThat(resultConsumer.poll(), nullValue())
    }
}
