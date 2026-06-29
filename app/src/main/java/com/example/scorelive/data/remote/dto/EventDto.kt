package com.example.scorelive.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.scorelive.domain.model.EventType
import com.example.scorelive.domain.model.MatchEvent
import com.example.scorelive.domain.model.Team

data class EventsResponseDto(
    @SerializedName("response")
    val response: List<EventItemDto>
)

data class EventItemDto(
    @SerializedName("time")
    val time: EventTimeDto,
    @SerializedName("team")
    val team: TeamDto,
    @SerializedName("player")
    val player: EventPlayerDto,
    @SerializedName("assist")
    val assist: EventPlayerDto?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("detail")
    val detail: String?
)

data class EventTimeDto(
    @SerializedName("elapsed")
    val elapsed: Int,
    @SerializedName("extra")
    val extra: Int?
)

data class EventPlayerDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
)

// EventItemDto → MatchEvent
fun EventItemDto.toMatchEvent(): MatchEvent {
    val safeDetail = detail ?: ""
    return MatchEvent(
        minute = time.elapsed,
        team = Team(
            id = team.id,
            name = team.name ?: "",
            logoUrl = team.logo ?: ""
        ),
        playerName = player.name ?: "",
        assistName = assist?.name,
        type = EventType.fromString(type ?: "", safeDetail),
        detail = safeDetail
    )
}