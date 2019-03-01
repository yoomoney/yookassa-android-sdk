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

package ru.yandex.money.android.sdk.model

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

// Arch components

//        Business logic                    Presentation logic
//|-----------------------------||---------------------------------------|
// InputModel -> UseCase -> OutputModel -> Presenter -> ViewModel -> View
//                  |                                                 ^
//                  └-----> Exception -> ErrorPresenter -> ViewModel -┘

// Util classes
private val asyncExecutor = Executors.newCachedThreadPool()

internal typealias UseCase<I, O> = (I) -> O

internal typealias Presenter<I, O> = (I) -> O

internal typealias ErrorPresenter = Presenter<Exception, CharSequence>

internal typealias Executor = (() -> Unit) -> Unit

internal typealias ViewModel = Any

/**
 * Execute given [useCase] on [bgExecutor] and deliver state to [resultConsumer]
 *
 * [I] - controller argument
 * [O] - output model
 * [V] - view_model
 */
internal class Controller<in I : Any, O : Any, out V : Any>(
    private val name: String,
    private val useCase: UseCase<I, O>,
    private val presenter: Presenter<O, V>,
    private val errorPresenter: Presenter<Exception, V>,
    private val progressPresenter: Presenter<Unit, V>?,
    private val resultConsumer: (ViewModel) -> Unit,
    private val logger: (String, Exception?) -> Unit,
    private val bgExecutor: Executor = { asyncExecutor.execute(it) }
) : (I) -> Unit {

    private var argument: I? = null
    private var cancellationSignal: AtomicBoolean? = null
    private var running: Boolean = false

    override fun invoke(inputModel: I) {
        if (!running || cancellationSignal?.get() == true) {
            log("invoke with $inputModel")

            running = true

            val myCancellationSignal = AtomicBoolean(false)

            cancellationSignal = myCancellationSignal
            argument = inputModel
            bgExecutor {
                progressPresenter?.invoke(Unit)?.also {
                    log("progress: $it")
                    resultConsumer(it)
                } ?: log("no progress")

                if (myCancellationSignal.get()) {
                    log("cancelled")
                    return@bgExecutor
                }

                val viewModel = try {
                    try {
                        val outputModel = useCase(inputModel)
                        log("output: $outputModel")
                        presenter(outputModel)
                    } catch (e: SdkException) {
                        log("error: ${e.message}", e)
                        errorPresenter(e)
                    }
                } catch (e: Exception) {
                    UnhandledException(e).also {
                        log("unhandled error: ${e.message}", it)
                    }
                }

                log("viewModel: $viewModel")

                if (myCancellationSignal.get()) {
                    log("cancelled")
                    return@bgExecutor
                }

                resultConsumer(viewModel)
                running = false
            }
        } else {
            log("cannot invoke because running and not cancelled")
        }
    }

    fun retry() {
        log("retry")

        val inputModel = checkNotNull(argument) { "argument should be present on retry" }
        invoke(inputModel)
    }

    fun reset() {
        log("reset")

        argument = null
        cancellationSignal?.set(true)
        cancellationSignal = null
        running = false
    }

    private fun log(message: String, e: Exception? = null) {
        logger("$name: $message", e)
    }
}

internal class UnhandledException(cause: Throwable?) : Exception(cause)

internal class StateHolder(private val uiExecutor: Executor) {

    private val stateListeners = mutableMapOf<KClass<*>, MutableList<Any>>()
    private var lastState: ViewModel? = null

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : ViewModel> onEvent(event: T) {
        lastState = event
        stateListeners
            .filterKeys { it.java.isInstance(event) }
            .values.asSequence()
            .flatten()
            .forEach { uiExecutor { (it as (T) -> Unit)(event) } }
    }

    inline operator fun <reified T : ViewModel> plusAssign(noinline listener: (T) -> Unit) {
        stateListeners.getOrPut(T::class, ::mutableListOf) += listener
        (lastState as? T)?.also {
            uiExecutor { listener(it) }
        }
    }

    inline operator fun <reified T : ViewModel> minusAssign(noinline listener: (T) -> Unit) {
        stateListeners.getOrPut(T::class, ::mutableListOf) -= listener
    }

    fun reset() {
        lastState = null
    }
}
