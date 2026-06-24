package com.example.scorelive.domain.repository

import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FootballRepository {

    fun getLeaguesFromDb(): Flow<List<League>>
    fun getMatchesByLeagueFromDb(leagueId: Int): Flow<List<Match>>

    // DB reads
    fun getLiveMatchesFromDb(): Flow<List<Match>>
    fun getTodayMatchesFromDb(): Flow<List<Match>>
    fun getMatchByIdFromDb(matchId: Int): Flow<Match?>
    fun getMatchesByDateFromDb(date: String): Flow<List<Match>>

    // API → Room
    suspend fun fetchLiveMatches(): NetworkResult<Unit>
    suspend fun fetchTodayMatches(date: String): NetworkResult<Unit>
    suspend fun fetchMatchesByDate(date: String): NetworkResult<Unit>

    // match detail (not cached)
    suspend fun fetchMatchEvents(matchId: Int): NetworkResult<List<MatchEvent>>
    suspend fun fetchLineups(matchId: Int): NetworkResult<List<Lineup>>
    suspend fun fetchStatistics(matchId: Int): NetworkResult<List<FixtureStat>>
    suspend fun fetchH2H(homeTeamId: Int, awayTeamId: Int): NetworkResult<List<Match>>
    suspend fun fetchStandings(leagueId: Int, season: Int): NetworkResult<List<Standing>>

    // favorites
    fun getFavoriteIds(): Flow<List<Int>>
    fun isFavorite(matchId: Int): Flow<Boolean>
    suspend fun addFavorite(matchId: Int)
    suspend fun removeFavorite(matchId: Int)

    // competition detail
    suspend fun fetchLeagueSeason(leagueId: Int): NetworkResult<Int>
    suspend fun fetchLeagueFixtures(leagueId: Int, season: Int): NetworkResult<List<Match>>
    suspend fun fetchTopScorers(leagueId: Int, season: Int): NetworkResult<List<TopPlayer>>
    suspend fun fetchTopAssists(leagueId: Int, season: Int): NetworkResult<List<TopPlayer>>

    // search
    suspend fun searchTeams(query: String): NetworkResult<List<Team>>
}