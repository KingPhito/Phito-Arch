package com.ralphdugue.phitoarch.mvi

import kotlinx.coroutines.flow.Flow

interface BaseViewState

interface BaseIntent

interface BaseIntentHandler<T : BaseIntent, R : BaseViewState> {

    fun process(event: T, currentState: R): Flow<R>
}



