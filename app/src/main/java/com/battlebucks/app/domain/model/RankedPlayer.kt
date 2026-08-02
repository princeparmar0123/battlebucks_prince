package com.battlebucks.app.domain.model

data class RankedPlayer(
    val player: Player,
    val rank: Int,
    val isHighlighted: Boolean = false
)
