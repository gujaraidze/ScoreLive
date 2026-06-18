package com.example.scorelive.domain.model

data class MatchEvent(
    val minute: Int,
    val team: Team,
    val playerName: String,
    val assistName: String?,
    val type: EventType,
    val detail: String
)

enum class EventType {
    GOAL,
    OWN_GOAL,
    PENALTY,
    YELLOW_CARD,
    RED_CARD,
    YELLOW_RED_CARD,
    SUBSTITUTION,
    VAR;

    companion object {
        fun fromString(type: String, detail: String): EventType {
            return when (type) {
                "Goal" -> when (detail) {
                    "Own Goal" -> OWN_GOAL
                    "Penalty" -> PENALTY
                    else -> GOAL
                }
                "Card" -> when (detail) {
                    "Yellow Card" -> YELLOW_CARD
                    "Red Card" -> RED_CARD
                    "Yellow Red Card" -> YELLOW_RED_CARD
                    else -> YELLOW_CARD
                }
                "subst" -> SUBSTITUTION
                "Var" -> VAR
                else -> GOAL
            }
        }
    }
}