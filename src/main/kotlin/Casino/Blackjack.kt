package trab.casino

class Blackjack {
    private val deck = Deck()
    private val dealerHand = mutableListOf<Card>()
    private val playerHand = mutableListOf<Card>()
    private var gameOver = false

    init {
        deck.shuffle()
    }

    fun startGame(): BlackjackGameState {
        resetGame()
        dealerHand.add(deck.cards.removeFirst())
        dealerHand.add(deck.cards.removeFirst())
        playerHand.add(deck.cards.removeFirst())
        playerHand.add(deck.cards.removeFirst())

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = listOf(dealerHand.first()), // Only show the first card
            dealerTotal = calculateHandValue(listOf(dealerHand.first())), // Only show the value of the first card
            playerHand = playerHand,
            playerTotal = calculateHandValue(playerHand),
            gameOver = gameOver
        )
    }

    fun hit(): BlackjackGameState {
        if (gameOver) return BlackjackGameState(
            dealerFirstCard = dealerHand.firstOrNull(),
            gameState = "Game is already over. Please restart.",
            gameOver = true
        )

        playerHand.add(deck.cards.removeFirst())
        val playerTotal = calculateHandValue(playerHand)

        if (playerTotal > 21) {
            gameOver = true
            return BlackjackGameState(
                dealerFirstCard = dealerHand.first(),
                dealerHand = dealerHand, // Reveal full hand on game over
                playerHand = playerHand,
                playerTotal = playerTotal,
                dealerTotal = calculateHandValue(dealerHand),
                gameState = "Bust! You lose.",
                gameOver = gameOver
            )
        }

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = listOf(dealerHand.first()), // Keep showing only the first card
            playerHand = playerHand,
            playerTotal = playerTotal,
            dealerTotal = calculateHandValue(listOf(dealerHand.first())), // Only show the first card's value
            gameOver = gameOver
        )
    }

    fun stand(): BlackjackGameState {
        if (gameOver) return BlackjackGameState(
            dealerFirstCard = null,
            gameState = "Game is already over. Please restart.",
            gameOver = true
        )

        while (calculateHandValue(dealerHand) < 17) {
            dealerHand.add(deck.cards.removeFirst())
        }

        val dealerTotal = calculateHandValue(dealerHand)
        val playerTotal = calculateHandValue(playerHand)

        gameOver = true
        val result = when {
            dealerTotal > 21 || playerTotal > dealerTotal -> "You win!"
            playerTotal < dealerTotal -> "You lose!"
            else -> "It's a tie!"
        }

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = dealerHand, // Reveal full hand after stand
            dealerTotal = dealerTotal,
            playerHand = playerHand,
            playerTotal = playerTotal,
            gameState = result,
            gameOver = gameOver
        )
    }

    fun restartGame(): BlackjackGameState {
        deck.shuffle()
        return startGame()
    }

    private fun resetGame() {
        dealerHand.clear()
        playerHand.clear()
        gameOver = false
        deck.cards = Deck().cards // Refill the deck with a new set of cards
        deck.shuffle() // Shuffle the new deck
    }

    private fun calculateHandValue(hand: List<Card>): Int {
        var total = 0
        var aces = 0

        for (card in hand) {
            total += when (card.rank) {
                "Ace" -> {
                    aces++
                    11
                }
                "King", "Queen", "Jack" -> 10
                else -> card.rank.toInt()
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }

        return total
    }
}