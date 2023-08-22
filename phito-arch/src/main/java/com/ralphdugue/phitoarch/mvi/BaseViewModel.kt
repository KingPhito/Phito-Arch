package com.ralphdugue.phitoarch.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel<T : BaseIntent, R : BaseViewState>(
    private val eventHandler: BaseIntentHandler<T, R>,
    private val ioDispatcher: CoroutineDispatcher
)   : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<R> = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _state.value
    )

    fun onEvent(event: T) {
        eventHandler.process(event, state.value)
            .onEach(::emitState)
            .catch { errorState(it) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    abstract fun initialState(): R

    abstract fun errorState(throwable: Throwable)

    private fun emitState(state: R) = updateState { state }

    private fun updateState(newState: (currentState: R) -> R) =
        synchronized(_state) {
            _state.value = newState(state.value)
        }

}