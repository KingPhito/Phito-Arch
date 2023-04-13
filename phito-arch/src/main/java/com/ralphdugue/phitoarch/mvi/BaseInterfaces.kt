package com.ralphdugue.phitoarch.mvi

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow

sealed class ViewState(val error: Throwable? = null) {
    abstract fun <R: ViewState> copy(
        error: Throwable? = null,
        vararg fields: Any
    ): R
}

interface BaseIntent

interface BaseIntentHandler<T : BaseIntent, R : ViewState> {

    fun process(event: T, currentState: R): Flow<R>
}


interface StatefulModel<T: ViewState> {
    val state: State<T>
}

class StateMutator<T : ViewState>(initialState: T) {

    private val _observable: MutableState<T> = mutableStateOf(initialState)
    val observable: State<T> = _observable

    init {
        _observable.value = initialState
    }

    fun updateState(newState: (currentState: T) -> T) =
        synchronized(observable.value) {
            _observable.value = newState(observable.value)
        }
}