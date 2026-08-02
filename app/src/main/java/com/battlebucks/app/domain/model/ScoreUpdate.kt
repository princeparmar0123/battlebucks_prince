package com.battlebucks.app.domain.model

data class ScoreUpdate(
    val playerId: Int,
    val username: String,
    val newScore: Long
)
