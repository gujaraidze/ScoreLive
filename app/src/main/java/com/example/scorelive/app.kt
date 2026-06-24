package com.example.scorelive

import android.app.Application
import com.example.scorelive.data.local.AppDatabase
import com.example.scorelive.data.remote.RetrofitClient
import com.example.scorelive.data.repository.FootballRepositoryImpl
import com.example.scorelive.data.repository.MockRepository
import com.example.scorelive.domain.repository.FootballRepository

class App : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: FootballRepository by lazy {
        if (USE_MOCK) {
            MockRepository()
        } else {
            FootballRepositoryImpl(
                matchDao = database.matchDao(),
                favoriteDao = database.favoriteDao(),
                apiService = RetrofitClient.apiService
            )
        }
    }

    companion object {
        // set to true to use fake data — zero API calls, safe to rebuild freely
        // set to false to use the real API-Football endpoint
        const val USE_MOCK = false
    }
}