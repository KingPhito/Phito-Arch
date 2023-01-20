package com.ralphdugue.phitoarch.mvi

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow

interface ViewState {

    data class Error(val message: String) : ViewState
}

interface BaseIntent

interface BaseIntentHandler<T : BaseIntent> {

    fun process(event: T, currentState: ViewState): Flow<ViewState>
}


interface StatefulModel {
    val state: State<ViewState>
}

class StateMutator(initialState: ViewState) {

    private val _observable = mutableStateOf(initialState)
    val observable: State<ViewState> = _observable

    init {
        _observable.value = initialState
    }

    fun updateState(newState: (currentState: ViewState) -> ViewState) =
        synchronized(observable.value) {
            _observable.value = newState(observable.value)
        }
}