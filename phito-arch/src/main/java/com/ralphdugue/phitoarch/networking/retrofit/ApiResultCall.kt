package com.ralphdugue.phitoarch.networking.retrofit

import com.ralphdugue.phitoarch.networking.ApiError
import com.ralphdugue.phitoarch.networking.ApiException
import com.ralphdugue.phitoarch.networking.ApiResult
import com.ralphdugue.phitoarch.networking.ApiSuccess
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

fun <T : Any> handleRetrofitCall(
    execute: () -> Response<T>
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

class ApiResultCall<T : Any>(
    private val proxy: Call<T>
) : Call<ApiResult<T>> {

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val networkResult = handleRetrofitCall { response }
                callback.onResponse(this@ApiResultCall, Response.success(networkResult))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val networkResult = ApiException<T>(t)
                callback.onResponse(this@ApiResultCall, Response.success(networkResult))
            }
        })
    }

    override fun execute(): Response<ApiResult<T>> = throw NotImplementedError()
    override fun clone(): Call<ApiResult<T>> = ApiResultCall(proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() { proxy.cancel() }
}