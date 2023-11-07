package com.ralphdugue.phitoarch.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<Event : BaseEvent, State : BaseViewState, Effect : BaseEffect>(
    private val ioDispatcher: CoroutineDispatcher
)  : ViewModel() {

    private val initialState: State by lazy { createInitialState() }
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _state.value
    )

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _effect : Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    fun onEvent(event: Event) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun subscribeToEvents() {
        events
            .onEach { event -> updateState { handleEvent(event) } }
            .catch { setEffect(createEffect(it)) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    protected abstract fun createInitialState(): State

    protected abstract suspend fun handleEvent(event: Event): State

    protected abstract fun createEffect(throwable: Throwable): Effect

    private fun updateState(newState: suspend (currentState: State) -> State) =
        synchronized(_state) {
            viewModelScope.launch { _state.update { newState(it) } }
        }

    private suspend fun setEffect(effect: Effect) {  _effect.send(effect) }
}