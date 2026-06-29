package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.scorelive.domain.model.Lineup
import com.example.scorelive.domain.model.Player
import com.example.scorelive.domain.model.Team

data class LineupsResponseDto(
    @SerializedName("response")
    val response: List<LineupItemDto>
)

data class LineupItemDto(
    @SerializedName("team")
    val team: TeamDto,
    @SerializedName("coach")
    val coach: CoachDto? = null,
    @SerializedName("formation")
    val formation: String?,
    // nullable + default empty list — API omits these fields before kickoff
    @SerializedName("startXI")
    val startXI: List<LineupPlayerDto>? = null,
    @SerializedName("substitutes")
    val substitutes: List<LineupPlayerDto>? = null
)

data class CoachDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("photo")
    val photo: String?
)

data class LineupPlayerDto(
    @SerializedName("player")
    val player: LineupPlayerInfoDto
)

data class LineupPlayerInfoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    // also nullable — be defensive on every field that crashed us once
    @SerializedName("number")
    val number: Int? = null,
    @SerializedName("pos")
    val pos: String? = null,
    // "row:col" pitch position, e.g. "4:2" — only present for startXI, null for substitutes
    @SerializedName("grid")
    val grid: String? = null
)

fun LineupItemDto.toLineup(): Lineup {
    return Lineup(
        team = Team(
            id = team.id,
            name = team.name ?: "",
            logoUrl = team.logo ?: ""
        ),
        formation = formation ?: "Unknown",
        coachName = coach?.name,
        // safe call + orEmpty — never crashes even if API sends null
        startXI = startXI?.map { it.player.toPlayer() }.orEmpty(),
        substitutes = substitutes?.map { it.player.toPlayer() }.orEmpty()
    )
}

fun LineupPlayerInfoDto.toPlayer(): Player {
    return Player(
        id = id,
        name = name,
        number = number ?: 0,
        position = pos ?: "",
        gridRow = grid?.substringBefore(":")?.toIntOrNull(),
        gridCol = grid?.substringAfter(":")?.toIntOrNull()
    )
}