package com.example.scorelive.domain.model

data class MatchDetail(
    val match: Match,
    val events: List<MatchEvent> = emptyList(),
    val lineups: List<Lineup> = emptyList(),
    val statistics: List<FixtureStat> = emptyList(),
    val h2h: List<Match> = emptyList(),
    val standings: List<Standing> = emptyList()
)