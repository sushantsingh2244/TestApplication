package com.sushi.testproject.common

import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val modified = response.newBuilder()
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .build()

        return modified
    }

}