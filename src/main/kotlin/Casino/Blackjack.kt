package trab.casino

import kotlin.compareTo

class Blackjack {
    private val deck = Deck()
    private val dealerHand = mutableListOf<Card>()
    private val playerHand = mutableListOf<Card>()
    private var gameOver = false

    init {
        deck.shuffle()
    }

    fun startGame(deckStyle: String): BlackjackGameState {
        resetGame()
        dealerHand.add(deck.cards.removeFirst())
        dealerHand.add(deck.cards.removeFirst())
        playerHand.add(deck.cards.removeFirst())
        playerHand.add(deck.cards.removeFirst())

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = dealerHand.map { it.toCardWithImage(deckStyle) },
            dealerTotal = calculateHandValue(listOf(dealerHand.first())),
            playerHand = playerHand.map { it.toCardWithImage(deckStyle) },
            playerTotal = calculateHandValue(playerHand),
            gameOver = gameOver
        )
    }

    fun hit(deckStyle: String): BlackjackGameState {
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
                dealerHand = dealerHand.map { it.toCardWithImage(deckStyle) },
                playerHand = playerHand.map { it.toCardWithImage(deckStyle) },
                playerTotal = playerTotal,
                dealerTotal = calculateHandValue(dealerHand),
                gameState = "Bust! You lose.",
                gameOver = gameOver
            )
        }

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = dealerHand.map { it.toCardWithImage(deckStyle) },
            playerHand = playerHand.map { it.toCardWithImage(deckStyle) },
            playerTotal = playerTotal,
            dealerTotal = calculateHandValue(listOf(dealerHand.first())),
            gameOver = gameOver
        )
    }

    fun stand(deckStyle: String): BlackjackGameState {
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

        val playerHasBlackjack = playerHand.size == 2 &&
                ((playerHand[0].rank == "Ace" && playerHand[1].rank in listOf("10", "Jack", "Queen", "King")) ||
                        (playerHand[1].rank == "Ace" && playerHand[0].rank in listOf("10", "Jack", "Queen", "King")))

        val result = when {
            playerHasBlackjack && (dealerTotal > 21 || playerTotal > dealerTotal) -> "Blackjack! You win 3x!"
            dealerTotal > 21 || playerTotal > dealerTotal -> "You win!"
            playerTotal < dealerTotal -> "You lose!"
            else -> "It's a tie!"
        }

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = dealerHand.map { it.toCardWithImage(deckStyle) },
            dealerTotal = dealerTotal,
            playerHand = playerHand.map { it.toCardWithImage(deckStyle) },
            playerTotal = playerTotal,
            gameState = result,
            gameOver = gameOver
        )
    }

    fun restartGame(deckStyle: String): BlackjackGameState {
        deck.shuffle()
        return startGame(deckStyle)
    }

    private fun resetGame() {
        dealerHand.clear()
        playerHand.clear()
        gameOver = false
        deck.cards = Deck().cards
        deck.shuffle()
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