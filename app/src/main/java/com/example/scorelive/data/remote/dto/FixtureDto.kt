package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FixturesResponseDto(
    @SerializedName("response")
    val response: List<FixtureResponseItemDto>
)

data class FixtureResponseItemDto(
    @SerializedName("fixture")
    val fixture: FixtureInfoDto,
    @SerializedName("league")
    val league: LeagueDto,
    @SerializedName("teams")
    val teams: TeamsDto,
    @SerializedName("goals")
    val goals: GoalsDto
)

data class FixtureInfoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("date")
    val date: String,
    @SerializedName("status")
    val status: FixtureStatusDto
)

data class FixtureStatusDto(
    @SerializedName("long")
    val long: String,
    @SerializedName("short")
    val short: String,
    @SerializedName("elapsed")
    val elapsed: Int?
)

data class LeagueDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("logo")
    val logo: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("season")
    val season: Int? = null
)

data class TeamsDto(
    @SerializedName("home")
    val home: TeamDto,
    @SerializedName("away")
    val away: TeamDto
)

data class TeamDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("logo")
    val logo: String
)

data class GoalsDto(
    @SerializedName("home")
    val home: Int?,
    @SerializedName("away")
    val away: Int?
)