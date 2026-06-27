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
    val date: String,
    // raw API status code ("HT", "1H", "2H", "P", "BT"...) — kept so the UI can show
    // half-time/penalties instead of a frozen minute number. Defaults to "" so mock/
    // placeholder Match() constructions don't need to set it.
    val statusShort: String = ""
)