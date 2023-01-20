package com.ralphdugue.phitoarch.networking

import android.util.Log
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import retrofit2.HttpException
import retrofit2.Response

sealed interface ApiResult<T : Any>

class ApiSuccess<T : Any>(val data: T) : ApiResult<T>
class ApiError<T : Any>(val message: String?) : ApiResult<T>
class ApiException<T : Any>(val e: Throwable) : ApiResult<T>


suspend fun <T : Any> handleRetrofitCall(
    execute: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiSuccess(body)
        } else {
            ApiError(message = response.message())
        }
    } catch (e: HttpException) {
        ApiError(message = e.message())
    } catch (e: Throwable) {
        ApiException(e)
    }
}

suspend fun <T : Operation.Data> handleApolloCall(
    execute: () -> ApolloCall<T>
): ApiResult<T> {
    return try {
        val response = execute().execute()
        if (response.hasErrors()) {
            var errors = ""
            response.errors?.forEach { errors += "${it.message}\n" }
            ApiError(message = errors)
        } else {
            ApiSuccess(response.dataAssertNoErrors)
        }
    } catch (e: ApolloException) {
        Log.d("Apollo Query Exception", e.stackTraceToString())
        ApiException(e)
    }
}