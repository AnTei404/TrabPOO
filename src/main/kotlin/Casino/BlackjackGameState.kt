package trab.casino

data class BlackjackGameState(
    val dealerFirstCard: Card?,
    val dealerHand: List<Card> = emptyList(),
    val dealerTotal: Int = 0,
    val playerHand: List<Card> = emptyList(),
    val playerTotal: Int = 0,
    val gameState: String? = null,
    val gameOver: Boolean = false
)