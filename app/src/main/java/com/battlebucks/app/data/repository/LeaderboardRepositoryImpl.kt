package com.battlebucks.app.data.repository

import com.battlebucks.app.data.engine.GameEngine
import com.battlebucks.app.di.ApplicationScope
import com.battlebucks.app.domain.model.Player
import com.battlebucks.app.domain.model.RankedPlayer
import com.battlebucks.app.domain.repository.LeaderboardRepository
import com.battlebucks.app.utils.RankingCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepositoryImpl @Inject constructor(
    private val gameEngine: GameEngine,
    @ApplicationScope private val appScope: CoroutineScope
) : LeaderboardRepository {

    private val playerMap = LinkedHashMap<Int, Player>()
    private val _leaderboard = MutableStateFlow<List<RankedPlayer>>(emptyList())
    private var collectJob: Job? = null

    init {
        syncFromEngine()
        _leaderboard.value = RankingCalculator.assignRanks(playerMap.values.toList())
    }

    override fun observeLeaderboard(): Flow<List<RankedPlayer>> = _leaderboard.asStateFlow()

    override fun start() {
        if (collectJob?.isActive == true) {
            gameEngine.start()
            return
        }

        collectJob = appScope.launch {
            gameEngine.updates.collect { update ->
                val existing = playerMap[update.playerId]
                val player = existing?.copy(score = update.newScore)
                    ?: Player(update.playerId, update.username, update.newScore)

                playerMap[update.playerId] = player
                _leaderboard.update {
                    RankingCalculator.assignRanks(
                        players = playerMap.values.toList(),
                        highlightedId = update.playerId
                    )
                }
            }
        }
        gameEngine.start()
    }

    override fun stop() {
        gameEngine.stop()
        collectJob?.cancel()
        collectJob = null
    }

    private fun syncFromEngine() {
        playerMap.clear()
        gameEngine.snapshot.forEach { playerMap[it.id] = it }
    }
}
