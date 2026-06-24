package com.example.scorelive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.scorelive.domain.model.League
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchStatus
import com.example.scorelive.domain.model.Team

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey
    val id: Int,
    val homeTeamId: Int,
    val homeTeamName: String,
    val homeTeamLogo: String,
    val awayTeamId: Int,
    val awayTeamName: String,
    val awayTeamLogo: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String,
    val leagueCountry: String,
    val leagueSeason: Int?,
    val status: String,
    val minute: Int?,
    val date: String,
    val isLive: Boolean
)

// MatchEntity → Match (domain model)
fun MatchEntity.toMatch(): Match {
    return Match(
        id = id,
        homeTeam = Team(
            id = homeTeamId,
            name = homeTeamName,
            logoUrl = homeTeamLogo
        ),
        awayTeam = Team(
            id = awayTeamId,
            name = awayTeamName,
            logoUrl = awayTeamLogo
        ),
        homeScore = homeScore,
        awayScore = awayScore,
        league = League(
            id = leagueId,
            name = leagueName,
            logoUrl = leagueLogo,
            country = leagueCountry,
            season = leagueSeason
        ),
        status = MatchStatus.fromString(status),
        minute = minute,
        date = date
    )
}