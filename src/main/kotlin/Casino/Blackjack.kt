package trab.casino

import kotlin.compareTo

/**
 * Represents the current state of a Blackjack game.
 * 
 * This data class contains all information needed to render the game state
 * in the UI and determine the outcome of the game.
 * 
 * @property dealerFirstCard The dealer's first card (visible to the player)
 * @property dealerHand The dealer's complete hand (may be partially hidden during gameplay)
 * @property dealerTotal The total value of the dealer's visible cards
 * @property playerHand The player's complete hand
 * @property playerTotal The total value of the player's hand
 * @property playerHasBlackjack Whether the player has a blackjack (21 with first two cards)
 * @property playerBust Whether the player has busted (exceeded 21)
 * @property dealerBust Whether the dealer has busted (exceeded 21)
 * @property gameOver Whether the current game has ended
 */
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

/**
 * Implements the game logic for Blackjack.
 * 
 * This class manages:
 * - The deck of cards
 * - The dealer's and player's hands
 * - Game state and rules
 * - Card dealing and hand evaluation
 */
class Blackjack {
    /** The deck of cards used for the game */
    private val deck = Deck()

    /** The dealer's hand of cards */
    private val dealerHand = mutableListOf<Card>()

    /** The player's hand of cards */
    private val playerHand = mutableListOf<Card>()

    /** Flag indicating whether the current game has ended */
    private var gameOver = false

    /** Initialize the game by shuffling the deck */
    init {
        deck.shuffle()
    }

    /**
     * Starts a new game of Blackjack.
     * 
     * This method:
     * 1. Resets the game state
     * 2. Deals two cards to the dealer
     * 3. Deals two cards to the player
     * 4. Returns the initial game state
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The initial state of the game
     */
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
            playerHasBlackjack = false,
            playerBust = false,
            dealerBust = false,
            gameOver = gameOver
        )
    }

    /**
     * Performs a "hit" action, dealing one more card to the player.
     * 
     * This method:
     * 1. Checks if the game is already over
     * 2. Deals one card to the player if the game is still active
     * 3. Checks if the player has busted (exceeded 21)
     * 4. Updates the game state accordingly
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The updated state of the game after the hit
     */
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

    /**
     * Performs a "stand" action, ending the player's turn and playing the dealer's hand.
     * 
     * This method:
     * 1. Checks if the game is already over
     * 2. Plays the dealer's hand according to standard rules (dealer hits until 17 or higher)
     * 3. Determines the outcome of the game
     * 4. Updates the game state accordingly
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The final state of the game after the stand
     */
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

    /**
     * Restarts the game with a fresh deck.
     * 
     * This method:
     * 1. Shuffles the deck
     * 2. Starts a new game
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The initial state of the new game
     */
    fun restartGame(deckStyle: String): BlackjackGameState {
        deck.shuffle()
        return startGame(deckStyle)
    }

    /**
     * Resets the game to its initial state.
     * 
     * This private method:
     * 1. Clears the dealer's and player's hands
     * 2. Resets the game state
     * 3. Replaces the deck with a fresh one
     * 4. Shuffles the deck
     */
    private fun resetGame() {
        dealerHand.clear()
        playerHand.clear()
        gameOver = false
        deck.cards = Deck().cards
        deck.shuffle()
    }

    /**
     * Calculates the total value of a hand in Blackjack.
     * 
     * This method follows standard Blackjack rules:
     * - Number cards (2-10) are worth their face value
     * - Face cards (Jack, Queen, King) are worth 10
     * - Aces are worth 11, but can be reduced to 1 if the total would exceed 21
     * 
     * @param hand The list of cards to evaluate
     * @return The total value of the hand
     */
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
