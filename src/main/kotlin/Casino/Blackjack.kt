package trab.casino

import kotlin.compareTo


data class BlackjackGameState(
    val dealerFirstCard: Card?,
    val dealerHand: List<Map<String, String>> = emptyList(),
    val dealerTotal: Int = 0,
    val playerHand: List<Map<String, String>> = emptyList(),
    val playerTotal: Int = 0,
    val playerHasBlackjack: Boolean = false,
    val playerBust: Boolean = false,
    val dealerBust: Boolean = false,
    val gameOver: Boolean = false
)


class Blackjack {
    private val deck = Deck()
    private val dealerHand = mutableListOf<Card>()
    private val playerHand = mutableListOf<Card>()
    private var gameOver = false

    init {
        deck.shuffle()
    }

    fun startGame(deckStyle: String): BlackjackGameState {
        return resetGame(deckStyle)
    }

    // Alias for startGame to maintain backward compatibility
    fun restartGame(deckStyle: String): BlackjackGameState {
        return startGame(deckStyle)
    }

    fun hit(deckStyle: String): BlackjackGameState {
        if (gameOver) return BlackjackGameState(
            dealerFirstCard = dealerHand.firstOrNull(),
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
                playerBust = true,
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

        val dealerBust = dealerTotal > 21

        return BlackjackGameState(
            dealerFirstCard = dealerHand.first(),
            dealerHand = dealerHand.map { it.toCardWithImage(deckStyle) },
            dealerTotal = dealerTotal,
            playerHand = playerHand.map { it.toCardWithImage(deckStyle) },
            playerTotal = playerTotal,
            playerHasBlackjack = playerHasBlackjack,
            dealerBust = dealerBust,
            gameOver = gameOver
        )
    }

    private fun resetGame(deckStyle: String): BlackjackGameState {
        dealerHand.clear()
        playerHand.clear()
        gameOver = false
        deck.cards = Deck().cards
        deck.shuffle()

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
            playerHasBlackjack = false,
            playerBust = false,
            dealerBust = false,
            gameOver = gameOver
        )
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
