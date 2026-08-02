package com.battlebucks.app.domain.usecase

import com.battlebucks.app.domain.repository.LeaderboardRepository
import javax.inject.Inject

class StartGameUseCase @Inject constructor(
    private val repository: LeaderboardRepository
) {
    operator fun invoke() = repository.start()
}
