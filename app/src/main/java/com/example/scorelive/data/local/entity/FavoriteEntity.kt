package com.example.scorelive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// only stores the matchId — we fetch full match data from matches table
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val matchId: Int
)