package com.example.scorelive.domain.model

data class FixtureStat(
    val team: Team,
    val statistics: List<StatItem>
)

data class StatItem(
    val type: String,
    val value: String
)