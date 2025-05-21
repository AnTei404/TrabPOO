package trab.casino

import kotlinx.serialization.Serializable

@Serializable
data class BingoGameState(
    val userCard: BingoCard,
    val houseCards: List<BingoCard>,
    val drawnNumbers: List<Int>,
    val lastNumber: Int?,
    val userHasBingo: Boolean,
    val houseWinners: List<Int>,
    val tie: Boolean = false
)