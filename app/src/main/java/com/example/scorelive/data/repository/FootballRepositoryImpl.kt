package com.example.scorelive.data.repository

import com.example.scorelive.data.NetworkResult
import com.example.scorelive.data.makeApiCall
import com.example.scorelive.data.local.dao.FavoriteDao
import com.example.scorelive.data.local.dao.MatchDao
import com.example.scorelive.data.local.entity.FavoriteEntity
import com.example.scorelive.data.local.entity.toMatch
import com.example.scorelive.data.remote.FootballApiService
import com.example.scorelive.data.remote.dto.*
import com.example.scorelive.domain.model.*
import com.example.scorelive.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FootballRepositoryImpl(
    private val matchDao: MatchDao,
    private val favoriteDao: FavoriteDao,
    private val apiService: FootballApiService
) : FootballRepository {

    // eliminates the repeated when(result) { Success -> ... Failure -> Failure(message) } pattern
    private inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> =
        when (this) {
            is NetworkResult.Success -> NetworkResult.Success(transform(data))
            is NetworkResult.Failure -> NetworkResult.Failure(message)
        }

    override fun getLeaguesFromDb(): Flow<List<League>> =
        matchDao.getDistinctLeagues().map { rows ->
            rows.map { League(it.leagueId, it.leagueName, it.leagueLogo, it.leagueCountry) }
        }

    override fun getMatchesByLeagueFromDb(leagueId: Int): Flow<List<Match>> =
        matchDao.getMatchesByLeague(leagueId).map { it.map { e -> e.toMatch() } }

    // DB reads
    override fun getTodayMatchesFromDb() =
        matchDao.getAllMatches().map { it.map { e -> e.toMatch() } }

    override fun getMatchByIdFromDb(matchId: Int) =
        matchDao.getMatchById(matchId).map { it?.toMatch() }

    override fun getMatchesByDateFromDb(date: String) =
        matchDao.getMatchesByDate(date).map { it.map { e -> e.toMatch() } }

    // fixture fetches — save to Room, return Unit
    override suspend fun fetchMatchesByDate(date: String) =
        makeApiCall { apiService.getFixturesByDate(date) }.mapSuccess {
            matchDao.upsertAll(it.response.map { item -> item.toEntity() })
        }

    // match detail — fetch from API, return domain list (not cached)
    override suspend fun fetchMatchEvents(matchId: Int) =
        makeApiCall { apiService.getMatchEvents(matchId) }.mapSuccess {
            it.response.map { item -> item.toMatchEvent() }
        }

    override suspend fun fetchLineups(matchId: Int) =
        makeApiCall { apiService.getLineups(matchId) }.mapSuccess {
            it.response.map { item -> item.toLineup() }
        }

    override suspend fun fetchStatistics(matchId: Int) =
        makeApiCall { apiService.getStatistics(matchId) }.mapSuccess {
            it.response.map { item -> item.toFixtureStat() }
        }

    override suspend fun fetchH2H(homeTeamId: Int, awayTeamId: Int) =
        makeApiCall { apiService.getHeadToHead("$homeTeamId-$awayTeamId") }.mapSuccess {
            it.response.map { item -> item.toEntity().toMatch() }
        }

    override suspend fun fetchStandings(leagueId: Int, season: Int) =
        makeApiCall { apiService.getStandings(leagueId, season) }.mapSuccess {
            it.response.firstOrNull()?.league?.standings
                ?.flatten()?.map { item -> item.toStanding() } ?: emptyList()
        }

    // favorites
    override fun getFavoriteIds(): Flow<List<Int>> =
        favoriteDao.getAllFavorites().map { it.map { e -> e.matchId } }

    override fun isFavorite(matchId: Int) = favoriteDao.isFavorite(matchId)

    override suspend fun addFavorite(matchId: Int) =
        favoriteDao.addFavorite(FavoriteEntity(matchId))

    override suspend fun removeFavorite(matchId: Int) =
        favoriteDao.removeFavorite(FavoriteEntity(matchId))

    // in-memory cache so we don't call /leagues on every match detail open
    private val seasonCache = mutableMapOf<Int, Int>()

    // competition detail
    override suspend fun fetchLeagueSeason(leagueId: Int): NetworkResult<Int> {
        seasonCache[leagueId]?.let { return NetworkResult.Success(it) }
        return when (val result = makeApiCall { apiService.getLeagueInfo(leagueId) }) {
            is NetworkResult.Failure -> NetworkResult.Failure(result.message)
            is NetworkResult.Success -> {
                val season = result.data.currentSeason()
                if (season == null) {
                    NetworkResult.Failure("NOT_AVAILABLE_FREE_PLAN")
                } else {
                    seasonCache[leagueId] = season
                    NetworkResult.Success(season)
                }
            }
        }
    }

    // search
    override suspend fun searchTeams(query: String) =
        makeApiCall { apiService.searchTeams(query) }.mapSuccess {
            it.response.map { item -> item.toTeam() }
        }
}