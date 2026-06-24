package com.example.scorelive.domain.model

enum class MatchStatus {
    LIVE,
    FINISHED,
    SCHEDULED,
    POSTPONED,
    CANCELLED;

    companion object {
        fun fromString(status: String): MatchStatus {
            return when (status) {
                "1H", "2H", "HT", "ET", "BT", "P", "INT", "LIVE" -> LIVE
                "FT", "AET", "PEN" -> FINISHED
                "NS", "TBD" -> SCHEDULED
                "PST" -> POSTPONED
                "CANC", "ABD", "AWD", "WO", "SUSP" -> CANCELLED
                else -> SCHEDULED
            }
        }
    }
}