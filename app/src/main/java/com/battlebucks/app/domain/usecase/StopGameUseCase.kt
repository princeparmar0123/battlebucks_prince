package com.battlebucks.app.domain.usecase

import com.battlebucks.app.domain.repository.LeaderboardRepository
import javax.inject.Inject

class StopGameUseCase @Inject constructor(
    private val repository: LeaderboardRepository
) {
    operator fun invoke() = repository.stop()
}
