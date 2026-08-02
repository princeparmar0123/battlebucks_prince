package com.battlebucks.app.domain.usecase

import com.battlebucks.app.domain.model.RankedPlayer
import com.battlebucks.app.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLeaderboardUseCase @Inject constructor(
    private val repository: LeaderboardRepository
) {
    operator fun invoke(): Flow<List<RankedPlayer>> = repository.observeLeaderboard()
}
