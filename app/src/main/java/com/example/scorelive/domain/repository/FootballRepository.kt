package com.example.scorelive.domain.repository

import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchEvent
import com.example.scorelive.data.NetworkResult
import kotlinx.coroutines.flow.Flow

interface FootballRepository {

    fun getLiveMatchesFromDb(): Flow<List<Match>>

    fun getTodayMatchesFromDb(): Flow<List<Match>>

    fun getMatchByIdFromDb(matchId: Int): Flow<Match?>

    suspend fun fetchLiveMatches(): NetworkResult<Unit>

    suspend fun fetchTodayMatches(date: String): NetworkResult<Unit>

    suspend fun fetchMatchEvents(matchId: Int): NetworkResult<List<MatchEvent>>
}