package com.example.scorelive.data.remote

import com.example.scorelive.data.remote.dto.EventsResponseDto
import com.example.scorelive.data.remote.dto.FixturesResponseDto
import com.example.scorelive.data.remote.dto.LineupsResponseDto
import com.example.scorelive.data.remote.dto.StatisticsResponseDto
import com.example.scorelive.data.remote.dto.StandingsResponseDto
import com.example.scorelive.data.remote.dto.TeamsSearchResponseDto
import com.example.scorelive.data.remote.dto.LeagueInfoResponseDto
import com.example.scorelive.data.remote.dto.TopPlayersResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("live") live: String = "all"
    ): FixturesResponseDto

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String
    ): FixturesResponseDto

    @GET("fixtures")
    suspend fun getFixturesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int = 2026
    ): FixturesResponseDto

    @GET("fixtures/headtohead")
    suspend fun getHeadToHead(
        @Query("h2h") h2h: String
    ): FixturesResponseDto

    @GET("fixtures/events")
    suspend fun getMatchEvents(
        @Query("fixture") fixtureId: Int
    ): EventsResponseDto

    @GET("fixtures/lineups")
    suspend fun getLineups(
        @Query("fixture") fixtureId: Int
    ): LineupsResponseDto

    @GET("fixtures/statistics")
    suspend fun getStatistics(
        @Query("fixture") fixtureId: Int
    ): StatisticsResponseDto

    @GET("leagues")
    suspend fun getLeagueInfo(
        @Query("id") leagueId: Int
    ): LeagueInfoResponseDto

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): StandingsResponseDto

    @GET("players/topscorers")
    suspend fun getTopScorers(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): TopPlayersResponseDto

    @GET("players/topassists")
    suspend fun getTopAssists(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): TopPlayersResponseDto

    @GET("teams")
    suspend fun searchTeams(
        @Query("search") query: String
    ): TeamsSearchResponseDto
}