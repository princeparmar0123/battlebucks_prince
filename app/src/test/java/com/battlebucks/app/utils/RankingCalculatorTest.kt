package com.battlebucks.app.utils

import com.battlebucks.app.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class RankingCalculatorTest {

    @Test
    fun denseRanking_skipsRanksOnTies() {
        val players = listOf(
            Player(1, "Ace", 100),
            Player(2, "Bolt", 90),
            Player(3, "Chip", 90),
            Player(4, "Dash", 80)
        )

        val ranked = RankingCalculator.assignRanks(players)

        assertEquals(1, ranked[0].rank)
        assertEquals(2, ranked[1].rank)
        assertEquals(2, ranked[2].rank)
        assertEquals(4, ranked[3].rank)
    }

    @Test
    fun sameScore_sortedByUsername() {
        val players = listOf(
            Player(1, "Zulu", 50),
            Player(2, "Alpha", 50)
        )

        val ranked = RankingCalculator.assignRanks(players)

        assertEquals("Alpha", ranked[0].player.username)
        assertEquals("Zulu", ranked[1].player.username)
        assertEquals(1, ranked[0].rank)
        assertEquals(1, ranked[1].rank)
    }

    @Test
    fun highlight_marksCorrectPlayer() {
        val players = listOf(
            Player(10, "Nova", 200),
            Player(11, "Pixel", 150)
        )

        val ranked = RankingCalculator.assignRanks(players, highlightedId = 11)

        assertEquals(false, ranked[0].isHighlighted)
        assertEquals(true, ranked[1].isHighlighted)
    }
}
