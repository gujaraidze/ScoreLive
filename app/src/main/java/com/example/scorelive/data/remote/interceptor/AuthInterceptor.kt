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
        private const val API_KEY = "YOUR_API_KEY_HERE"
//
//
    }
}