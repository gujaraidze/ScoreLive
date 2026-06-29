package com.example.scorelive.data

import kotlinx.coroutines.delay

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure<out T>(val message: String) : NetworkResult<T>()
}

// API-Football free plan allows only 10 requests/MINUTE. Opening a match detail fires
// several calls at once (events, lineups, stats, h2h, season), so a burst can hit a 429
// "too many requests". Instead of failing outright, we honor the server's Retry-After
// header and retry a couple of times — so a rate-limited call recovers on its own once
// the per-minute window resets, instead of leaving the screen blank.
suspend fun <T> makeApiCall(
    maxRetries: Int = 2,
    call: suspend () -> T
): NetworkResult<T> {
    var attempt = 0
    while (true) {
        try {
            return NetworkResult.Success(call())
        } catch (e: retrofit2.HttpException) {
            // 429 = rate limited. Wait the server-suggested delay (capped) and retry.
            if (e.code() == 429 && attempt < maxRetries) {
                val retryAfterSec = e.response()?.headers()?.get("Retry-After")?.toIntOrNull() ?: 20
                val waitMs = (retryAfterSec.coerceIn(1, 25)) * 1000L
                attempt++
                delay(waitMs)
                continue
            }
            return NetworkResult.Failure(
                if (e.code() == 429) "RATE_LIMIT" else (e.message ?: "HTTP error")
            )
        } catch (e: java.io.IOException) {
            return NetworkResult.Failure("No internet connection")
        } catch (e: Exception) {
            return NetworkResult.Failure(e.message ?: "Unknown error")
        }
    }
}