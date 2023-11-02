package com.ralphdugue.phitoarch.networking.retrofit

import com.ralphdugue.phitoarch.networking.ApiResult
import retrofit2.CallAdapter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiResultCallAdapter(
    private val resultType: Type
) : CallAdapter<Type, ApiResultCall<Type>> {

    override fun responseType(): Type = resultType

    override fun adapt(call: retrofit2.Call<Type>): ApiResultCall<Type> {
        return ApiResultCall(call)
    }
}

class ApiResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: retrofit2.Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != ApiResultCall::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ApiResult::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return ApiResultCallAdapter(resultType)
    }
}