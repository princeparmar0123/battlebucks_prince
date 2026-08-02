package com.battlebucks.app.presentation.leaderboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battlebucks.app.domain.model.RankedPlayer
import com.battlebucks.app.domain.usecase.ObserveLeaderboardUseCase
import com.battlebucks.app.domain.usecase.StartGameUseCase
import com.battlebucks.app.domain.usecase.StopGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    observeLeaderboard: ObserveLeaderboardUseCase,
    private val startGame: StartGameUseCase,
    private val stopGame: StopGameUseCase
) : ViewModel() {

    val leaderboard: StateFlow<List<RankedPlayer>> = observeLeaderboard()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onScreenVisible() = startGame()

    fun onScreenHidden() = stopGame()

    override fun onCleared() {
        stopGame()
        super.onCleared()
    }
}
