package com.example.scorelive.domain.model

data class Lineup(
    val team: Team,
    val formation: String,
    val coachName: String? = null,
    val startXI: List<Player>,
    val substitutes: List<Player>
)