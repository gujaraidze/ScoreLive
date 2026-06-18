package com.example.scorelive.data.remote

import com.example.scorelive.data.remote.dto.EventsResponseDto
import com.example.scorelive.data.remote.dto.FixturesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    // live matches - home screen
    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("live") live: String = "all"
    ): FixturesResponseDto

    // fixtures by league and season - competition screen
    @GET("fixtures")
    suspend fun getFixturesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int = 2024
    ): FixturesResponseDto

    // today's fixtures - home screen
    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String
    ): FixturesResponseDto

    // events for match detail screen
    @GET("fixtures/events")
    suspend fun getMatchEvents(
        @Query("fixture") fixtureId: Int
    ): EventsResponseDto
}