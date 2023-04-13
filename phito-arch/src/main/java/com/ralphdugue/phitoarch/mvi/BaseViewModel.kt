package com.ralphdugue.phitoarch.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<T : BaseIntent, R : ViewState>(
    private val eventHandler: BaseIntentHandler<T, R>,
    private val ioDispatcher: CoroutineDispatcher
)   : ViewModel(), StatefulModel<R> {

    private val stateMutator by lazy { StateMutator(initialState()) }
    override val state = stateMutator.observable
    private val events = Channel<T>()

    init {
        events.receiveAsFlow()
            .onEach(::handleEvent)
            .launchIn(viewModelScope)
    }

    fun queueIntent(event: T) { events.trySend(event) }

    private fun handleEvent(event: T) {
        eventHandler.process(event, state.value)
            .onEach(::emitState)
            .catch { errorState(it) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    abstract fun initialState(): R

    private fun errorState(throwable: Throwable) {
        stateMutator.updateState { it.copy(error = throwable) }
    }

    private fun emitState(state: R) = stateMutator.updateState { state }


}