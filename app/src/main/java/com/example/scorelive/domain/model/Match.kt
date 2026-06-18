package com.example.scorelive.domain.model

data class Match(
    val id: Int,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val league: League,
    val status: MatchStatus,
    val minute: Int?,
    val date: String
)