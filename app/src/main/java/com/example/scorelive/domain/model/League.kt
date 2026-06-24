package com.example.scorelive.domain.model

data class League(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val country: String,
    val season: Int? = null
)