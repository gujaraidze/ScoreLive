package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.scorelive.domain.model.FixtureStat
import com.example.scorelive.domain.model.Standing
import com.example.scorelive.domain.model.StatItem
import com.example.scorelive.domain.model.Team

// ---- Statistics ----

data class StatisticsResponseDto(
    @SerializedName("response")
    val response: List<TeamStatisticsDto>
)

data class TeamStatisticsDto(
    @SerializedName("team")
    val team: TeamDto,
    @SerializedName("statistics")
    val statistics: List<StatItemDto>
)

data class StatItemDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("value")
    val value: Any? // API returns int, string, or null
)

fun TeamStatisticsDto.toFixtureStat(): FixtureStat {
    return FixtureStat(
        team = Team(
            id = team.id,
            name = team.name,
            logoUrl = team.logo
        ),
        statistics = statistics.map {
            StatItem(
                type = it.type,
                value = it.value?.toString() ?: "0"
            )
        }
    )
}

// ---- Standings ----

data class StandingsResponseDto(
    @SerializedName("response")
    val response: List<StandingsLeagueWrapperDto>
)

data class StandingsLeagueWrapperDto(
    @SerializedName("league")
    val league: StandingsLeagueDto
)

data class StandingsLeagueDto(
    @SerializedName("standings")
    val standings: List<List<StandingItemDto>>
)

data class StandingItemDto(
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("team")
    val team: TeamDto,
    @SerializedName("points")
    val points: Int,
    @SerializedName("goalsDiff")
    val goalsDiff: Int,
    @SerializedName("form")
    val form: String?,
    @SerializedName("group")
    val group: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("all")
    val all: StandingStatsDto
)

data class StandingStatsDto(
    @SerializedName("played")
    val played: Int,
    @SerializedName("win")
    val win: Int,
    @SerializedName("draw")
    val draw: Int,
    @SerializedName("lose")
    val lose: Int,
    @SerializedName("goals")
    val goals: StandingGoalsDto
)

data class StandingGoalsDto(
    @SerializedName("for")
    val goalsFor: Int,
    @SerializedName("against")
    val goalsAgainst: Int
)

fun StandingItemDto.toStanding(): Standing {
    return Standing(
        rank = rank,
        team = Team(
            id = team.id,
            name = team.name,
            logoUrl = team.logo
        ),
        played = all.played,
        won = all.win,
        drawn = all.draw,
        lost = all.lose,
        goalsFor = all.goals.goalsFor,
        goalsAgainst = all.goals.goalsAgainst,
        goalDifference = goalsDiff,
        points = points,
        form = form,
        group = group,
        zoneDescription = description
    )
}