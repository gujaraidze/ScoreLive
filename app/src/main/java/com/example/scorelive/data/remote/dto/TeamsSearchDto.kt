package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.scorelive.domain.model.Team

data class TeamsSearchResponseDto(
    @SerializedName("response")
    val response: List<TeamSearchItemDto>
)

data class TeamSearchItemDto(
    @SerializedName("team")
    val team: TeamSearchInfoDto,
    @SerializedName("venue")
    val venue: VenueDto?
)

data class TeamSearchInfoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("country")
    val country: String?
)

data class VenueDto(
    @SerializedName("name")
    val name: String?,
    @SerializedName("city")
    val city: String?
)

fun TeamSearchItemDto.toTeam(): Team {
    return Team(
        id = team.id,
        name = team.name ?: "",
        logoUrl = team.logo ?: ""
    )
}