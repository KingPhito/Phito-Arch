package com.ralphdugue.phitoarch.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<T : BaseIntent>(
    private val eventHandler: BaseIntentHandler<T>,
    private val ioDispatcher: CoroutineDispatcher
)   : ViewModel(), StatefulModel {

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

    abstract fun initialState(): ViewState

    private fun errorState(throwable: Throwable) = ViewState.Error(throwable.toString())

    private fun emitState(state: ViewState) = stateMutator.updateState { state }
}