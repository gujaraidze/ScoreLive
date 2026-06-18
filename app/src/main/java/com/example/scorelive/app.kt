package com.example.scorelive

import android.app.Application
import com.example.scorelive.data.local.AppDatabase
import com.example.scorelive.data.remote.RetrofitClient
import com.example.scorelive.data.repository.FootballRepositoryImpl
import com.example.scorelive.domain.repository.FootballRepository

class App : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: FootballRepository by lazy {
        FootballRepositoryImpl(
            matchDao = database.matchDao(),
            apiService = RetrofitClient.apiService
        )
    }
}