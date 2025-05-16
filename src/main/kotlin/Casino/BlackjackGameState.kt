package trab.casino

data class BlackjackGameState(
    val dealerFirstCard: Card?,
    val dealerHand: List<Map<String, String>> = emptyList(), // Updated type
    val dealerTotal: Int = 0,
    val playerHand: List<Map<String, String>> = emptyList(), // Updated type
    val playerTotal: Int = 0,
    val gameState: String? = null,
    val gameOver: Boolean = false
)