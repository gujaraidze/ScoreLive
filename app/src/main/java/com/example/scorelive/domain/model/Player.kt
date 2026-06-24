package com.example.scorelive.domain.model

data class Player(
    val id: Int,
    val name: String,
    val number: Int,
    val position: String,
    // pitch position for the formation view — row 1 is the goalkeeper, increasing
    // rows move toward attack; null for substitutes (API only sends grid for startXI)
    val gridRow: Int? = null,
    val gridCol: Int? = null
)