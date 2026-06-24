package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeagueInfoResponseDto(
    @SerializedName("response")
    val response: List<LeagueInfoItemDto>
)

data class LeagueInfoItemDto(
    @SerializedName("league")
    val league: LeagueInfoDto,
    @SerializedName("seasons")
    val seasons: List<LeagueSeasonDto>
)

data class LeagueInfoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String? // "League" or "Cup"
)

data class LeagueSeasonDto(
    @SerializedName("year")
    val year: Int,
    @SerializedName("start")
    val start: String?, // e.g. "2025-08-09"
    @SerializedName("end")
    val end: String?,   // e.g. "2026-05-24"
    @SerializedName("current")
    val current: Boolean,
    @SerializedName("coverage")
    val coverage: SeasonCoverageDto?
)

data class SeasonCoverageDto(
    @SerializedName("standings")
    val standings: Boolean,
    @SerializedName("top_scorers")
    val topScorers: Boolean,
    @SerializedName("top_assists")
    val topAssists: Boolean
)

// Free plan restriction: only seasons 2022-2024 are accessible for league data.
// Returns the season year if the current season is accessible, null otherwise.
// Callers should show "not available" rather than fetching wrong-season data.
private const val FREE_PLAN_MAX_SEASON = 2024

fun LeagueInfoResponseDto.currentSeason(): Int? {
    val item = response.firstOrNull() ?: return null
    val seasons = item.seasons

    // find what the actual current season is
    val currentSeason = seasons.firstOrNull { it.current }
        ?: seasons.maxByOrNull { it.year }
        ?: return null

    // if current season is within free plan range and has standings, use it
    if (currentSeason.year <= FREE_PLAN_MAX_SEASON && currentSeason.coverage?.standings == true) {
        return currentSeason.year
    }

    // current season is blocked (2025+) — return null so callers show "not available"
    // rather than silently showing last season's data which would be misleading
    return null
}