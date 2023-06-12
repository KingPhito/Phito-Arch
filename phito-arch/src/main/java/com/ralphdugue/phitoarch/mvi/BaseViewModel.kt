package com.ralphdugue.phitoarch.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseViewModel<T : BaseIntent, R : BaseViewState>(
    private val eventHandler: BaseIntentHandler<T, R>,
    private val ioDispatcher: CoroutineDispatcher
)   : ViewModel() {

    var state by mutableStateOf(initialState())
        private set

    private fun onEvent(event: T) {
        eventHandler.process(event, state)
            .onEach(::emitState)
            .catch { errorState(it) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    abstract fun initialState(): R

    abstract fun errorState(throwable: Throwable)

    private fun emitState(state: R) = updateState { state }

    private fun updateState(newState: (currentState: R) -> R) =
        synchronized(state) {
            state = newState(state)
        }

}