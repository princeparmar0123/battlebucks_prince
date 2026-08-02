package com.battlebucks.app.data.engine

import com.battlebucks.app.domain.model.Player
import com.battlebucks.app.domain.model.ScoreUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameEngine(
    private val scope: CoroutineScope,
    private val playerCount: Int = 25
) {
    private val random = Random.Default
    private val players = LinkedHashMap<Int, Player>()
    private var loopJob: Job? = null

    private val _updates = MutableSharedFlow<ScoreUpdate>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val updates: Flow<ScoreUpdate> = _updates.asSharedFlow()

    val snapshot: List<Player>
        get() = players.values.toList()

    init {
        seedPlayers()
    }

    fun start() {
        if (loopJob?.isActive == true) return

        loopJob = scope.launch {
            while (isActive) {
                delay(random.nextLong(MIN_INTERVAL_MS, MAX_INTERVAL_MS + 1))
                bumpRandomPlayer()
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    private fun seedPlayers() {
        players.clear()
        for (i in 1..playerCount) {
            val id = i
            players[id] = Player(
                id = id,
                username = if (id == CURRENT_PLAYER_ID) {
                    "Prince Parmar"
                } else {
                    USERNAMES[(i - 1) % USERNAMES.size]
                },
                score = random.nextLong(0, 500)
            )
        }
    }

    private suspend fun bumpRandomPlayer() {
        val keys = players.keys.toList()
        if (keys.isEmpty()) return

        val id = keys[random.nextInt(keys.size)]
        val current = players[id] ?: return
        val increment = random.nextLong(MIN_INCREMENT, MAX_INCREMENT + 1)
        val updated = current.copy(score = current.score + increment)
        players[id] = updated

        _updates.emit(
            ScoreUpdate(
                playerId = updated.id,
                username = updated.username,
                newScore = updated.score
            )
        )
    }

    companion object {
        private const val MIN_INTERVAL_MS = 500L
        private const val MAX_INTERVAL_MS = 2000L
        private const val MIN_INCREMENT = 1L
        private const val MAX_INCREMENT = 25L
        private const val CURRENT_PLAYER_ID = 12

        private val USERNAMES = listOf(
            "NovaStrike", "PixelHawk", "IronClaw", "ShadowByte", "BlitzFang",
            "CyberWolf", "FrostBolt", "RageViper", "NightRift", "StormAce",
            "QuantumKid", "DustRunner", "EchoBlade", "VenomDash", "CrimsonFox",
            "ZeroGravity", "TurboNest", "ApexDrift", "SilentCore", "HyperSpark",
            "GlitchKing", "RogueOrbit", "PulseRider", "DarkMatter", "NeonPulse",
            "SteelGhost", "RapidTide", "OmegaSlash", "VoltHunter", "PhantomAce",
            "LavaDrift", "SkyBreaker", "NitroWolf", "ColdSnap", "WarpRunner",
            "HexBlade", "PrimeShot", "AshVortex", "BlueComet", "RedCircuit",
            "JadeRogue", "SolarFang", "TidalCrash", "UltraByte", "MechaLynx",
            "VoidWalker", "ArcReactor", "FlameDart", "IceQuake", "BoltCraze"
        )
    }
}
