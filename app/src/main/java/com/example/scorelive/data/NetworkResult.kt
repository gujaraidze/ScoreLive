package com.example.scorelive.data

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure<out T>(val message: String) : NetworkResult<T>()
}

suspend fun <T> makeApiCall(call: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (e: retrofit2.HttpException) {
        NetworkResult.Failure(e.message ?: "HTTP error")
    } catch (e: java.io.IOException) {
        NetworkResult.Failure("No internet connection")
    } catch (e: Exception) {
        NetworkResult.Failure(e.message ?: "Unknown error")
    }
}