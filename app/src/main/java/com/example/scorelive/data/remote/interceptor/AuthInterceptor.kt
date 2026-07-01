package com.example.scorelive.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-apisports-key", API_KEY)
            .build()
        return chain.proceed(request)
    }

    companion object {
        private const val API_KEY = "fae3b5f3c6e6998e88584c49a03b3e75"

    }
}