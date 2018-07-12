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
import ru.yandex.money.android.sdk.AuthType
import ru.yandex.money.android.sdk.AuthTypeState
import ru.yandex.money.android.sdk.Presenter
import ru.yandex.money.android.sdk.SdkException
import ru.yandex.money.android.sdk.UseCase
import ru.yandex.money.android.sdk.ViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractErrorViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractFailViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractSuccessViewModel
import ru.yandex.money.android.sdk.logout.LogoutInputModel
import ru.yandex.money.android.sdk.on
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryInputModel
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryOutputModel
import ru.yandex.money.android.sdk.stubContractSuccessViewModel
import ru.yandex.money.android.sdk.waitUntilEmpty
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class SmsSessionRetryControllerTest {

    @[Rule JvmField]
    val timeout = Timeout(1, TimeUnit.MINUTES)

    private val states = LinkedBlockingQueue<ViewModel>()

    @Mock
    private lateinit var useCase: UseCase<SmsSessionRetryInputModel, SmsSessionRetryOutputModel>
    @Mock
    private lateinit var progressPresenter: Presenter<ProgressSmsSessionRetryViewModel, ContractSuccessViewModel>
    @Mock
    private lateinit var smsSessionRetryPresenter: Presenter<SmsSessionRetryOutputModel, ContractSuccessViewModel>
    @Mock
    private lateinit var errorPresenter: Presenter<Exception, ContractFailViewModel>

    private lateinit var controller: SmsSessionRetryController

    @Before
    fun setUp() {
        on(progressPresenter(ProgressSmsSessionRetryViewModel)).thenReturn(stubContractSuccessViewModel())

        controller = SmsSessionRetryController(
                smsSessionRetryUseCase = useCase,
                progressPresenter = progressPresenter,
                smsSessionRetryPresenter = smsSessionRetryPresenter,
                errorPresenter = errorPresenter,
                resultConsumer = { states.add(it) },
                logger = { _, _ ->  }
        )
    }

    @Test
    fun successPass() {
        // prepare
        val output = SmsSessionRetryOutputModel(AuthTypeState(AuthType.TOTP, 15))
        on(useCase(LogoutInputModel)).thenReturn(output)
        on(smsSessionRetryPresenter(output)).thenReturn(stubContractSuccessViewModel())

        // invoke
        controller(SmsSessionRetryInputModel)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, progressPresenter, smsSessionRetryPresenter, errorPresenter).apply {
            verify(progressPresenter).invoke(ProgressSmsSessionRetryViewModel)
            verify(useCase).invoke(SmsSessionRetryInputModel)
            verify(smsSessionRetryPresenter).invoke(output)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun failPass() {
        // prepare
        val exception = SdkException()
        on(useCase.invoke(SmsSessionRetryInputModel)).then { throw exception }
        on(errorPresenter(exception)).thenReturn(ContractErrorViewModel("err"))

        // invoke
        controller(SmsSessionRetryInputModel)

        // assert
        waitUntilEmpty(states)
        inOrder(useCase, progressPresenter, smsSessionRetryPresenter, errorPresenter).apply {
            verify(progressPresenter).invoke(ProgressSmsSessionRetryViewModel)
            verify(useCase).invoke(SmsSessionRetryInputModel)
            verify(errorPresenter).invoke(exception)
            verifyNoMoreInteractions()
        }
    }
}
