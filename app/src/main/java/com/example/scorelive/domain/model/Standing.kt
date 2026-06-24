package com.example.scorelive.domain.model

data class Standing(
    val rank: Int,
    val team: Team,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val form: String?,
    // e.g. "Eastern Conference", "Group A" — null for simple single-table leagues
    val group: String? = null,
    val zoneDescription: String? = null
)