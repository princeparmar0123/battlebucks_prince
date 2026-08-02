package com.battlebucks.app.domain.repository

import com.battlebucks.app.domain.model.RankedPlayer
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun observeLeaderboard(): Flow<List<RankedPlayer>>
    fun start()
    fun stop()
}
