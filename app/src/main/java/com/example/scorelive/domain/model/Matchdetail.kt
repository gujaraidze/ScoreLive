package com.example.scorelive.domain.model

data class MatchDetail(
    val match: Match,
    val events: List<MatchEvent>
)