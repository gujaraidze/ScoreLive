package com.example.scorelive.domain.model

data class TopPlayer(
    val id: Int,
    val name: String,
    val photoUrl: String?,
    val teamName: String,
    val teamLogoUrl: String,
    val goals: Int,
    val assists: Int
)