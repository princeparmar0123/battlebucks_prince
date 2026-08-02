package com.battlebucks.app.utils

import com.battlebucks.app.domain.model.Player
import com.battlebucks.app.domain.model.RankedPlayer

object RankingCalculator {

    fun assignRanks(
        players: List<Player>,
        highlightedId: Int? = null
    ): List<RankedPlayer> {
        if (players.isEmpty()) return emptyList()

        val sorted = players.sortedWith(
            compareByDescending<Player> { it.score }
                .thenBy { it.username.lowercase() }
        )

        val result = ArrayList<RankedPlayer>(sorted.size)
        var currentRank = 1

        for (i in sorted.indices) {
            // Skip ranks when scores are tied
            if (i > 0 && sorted[i].score != sorted[i - 1].score) {
                currentRank = i + 1
            }
            result.add(
                RankedPlayer(
                    player = sorted[i],
                    rank = currentRank,
                    isHighlighted = sorted[i].id == highlightedId
                )
            )
        }
        return result
    }
}
