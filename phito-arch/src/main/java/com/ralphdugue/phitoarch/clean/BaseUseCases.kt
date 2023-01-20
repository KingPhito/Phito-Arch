package com.ralphdugue.phitoarch.clean

import kotlinx.coroutines.flow.Flow

interface SuspendUseCase<P, T> {
    suspend fun execute(param: P): T
}

interface FlowUseCase<P, T> {
    fun execute(param: P): Flow<T>
}