package com.example.scorelive.data.repository

import com.example.scorelive.data.NetworkResult
import com.example.scorelive.data.makeApiCall
import com.example.scorelive.data.local.dao.MatchDao
import com.example.scorelive.data.local.entity.toMatch
import com.example.scorelive.data.remote.FootballApiService
import com.example.scorelive.data.remote.dto.toEntity
import com.example.scorelive.data.remote.dto.toMatchEvent
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchEvent
import com.example.scorelive.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FootballRepositoryImpl(
    private val matchDao: MatchDao,
    private val apiService: FootballApiService
) : FootballRepository {

    override fun getLiveMatchesFromDb(): Flow<List<Match>> {
        return matchDao.getLiveMatches().map { entities ->
            entities.map { it.toMatch() }
        }
    }

    override fun getTodayMatchesFromDb(): Flow<List<Match>> {
        return matchDao.getAllMatches().map { entities ->
            entities.map { it.toMatch() }
        }
    }

    override fun getMatchByIdFromDb(matchId: Int): Flow<Match?> {
        return matchDao.getMatchById(matchId).map { entity ->
            entity?.toMatch()
        }
    }

    override suspend fun fetchLiveMatches(): NetworkResult<Unit> {
        val result = makeApiCall { apiService.getLiveFixtures() }
        return when (result) {
            is NetworkResult.Success -> {
                val entities = result.data.response.map { it.toEntity() }
                matchDao.upsertAll(entities)
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Failure -> {
                NetworkResult.Failure(result.message)
            }
        }
    }

    override suspend fun fetchTodayMatches(date: String): NetworkResult<Unit> {
        val result = makeApiCall { apiService.getFixturesByDate(date) }
        return when (result) {
            is NetworkResult.Success -> {
                val entities = result.data.response.map { it.toEntity() }
                matchDao.upsertAll(entities)
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Failure -> {
                NetworkResult.Failure(result.message)
            }
        }
    }

    override suspend fun fetchMatchEvents(matchId: Int): NetworkResult<List<MatchEvent>> {
        val result = makeApiCall { apiService.getMatchEvents(matchId) }
        return when (result) {
            is NetworkResult.Success -> {
                val events = result.data.response.map { it.toMatchEvent() }
                NetworkResult.Success(events)
            }
            is NetworkResult.Failure -> {
                NetworkResult.Failure(result.message)
            }
        }
    }
}