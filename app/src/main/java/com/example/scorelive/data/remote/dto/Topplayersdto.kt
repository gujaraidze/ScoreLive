package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.scorelive.domain.model.TopPlayer

data class TopPlayersResponseDto(
    @SerializedName("response")
    val response: List<TopPlayerItemDto>
)

data class TopPlayerItemDto(
    @SerializedName("player")
    val player: TopPlayerInfoDto,
    @SerializedName("statistics")
    val statistics: List<TopPlayerStatDto>
)

data class TopPlayerInfoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("photo")
    val photo: String?
)

data class TopPlayerStatDto(
    @SerializedName("team")
    val team: TeamDto,
    @SerializedName("goals")
    val goals: TopPlayerGoalsDto?
)

data class TopPlayerGoalsDto(
    @SerializedName("total")
    val total: Int?,
    @SerializedName("assists")
    val assists: Int?
)

fun TopPlayerItemDto.toTopPlayer(): TopPlayer {
    val stat = statistics.firstOrNull()
    return TopPlayer(
        id = player.id,
        name = player.name,
        photoUrl = player.photo,
        teamName = stat?.team?.name ?: "",
        teamLogoUrl = stat?.team?.logo ?: "",
        goals = stat?.goals?.total ?: 0,
        assists = stat?.goals?.assists ?: 0
    )
}