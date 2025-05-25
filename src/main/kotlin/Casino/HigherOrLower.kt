package trab.casino

/**
 * Represents the current state of a Higher or Lower game.
 * 
 * This data class contains all information needed to render the game state
 * in the UI and determine the outcome of the game.
 * 
 * @property playerCard The player's current card
 * @property dealerCard The dealer's current card (may be hidden)
 * @property showDealerCard Whether the dealer's card is revealed
 * @property result The result message to display in the main game area
 * @property sideMenuResult The result message to display in the side menu
 * @property currentRound The current round number
 * @property totalRounds The total number of rounds in the current game
 * @property multiplier The current win multiplier
 * @property canLeaveOrAllIn Whether the player can choose to leave with winnings or go all-in
 * @property gameOver Whether the game has ended
 * @property roundCompleted Whether the current round is completed
 */
data class HigherOrLowerGameState(
    val playerCard: Map<String, String>?,
    val dealerCard: Map<String, String>?, // Hidden until after guess
    val showDealerCard: Boolean = false,
    val result: String? = null,
    val sideMenuResult: String? = null,
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val multiplier: Int = 1,
    val canLeaveOrAllIn: Boolean = false,
    val gameOver: Boolean = false,
    val roundCompleted: Boolean = false
)

/**
 * Implements the game logic for Higher or Lower.
 * 
 * In this game, the player is shown a card and must guess whether
 * the dealer's card will be higher or lower. The game consists of
 * multiple rounds, with increasing stakes and rewards.
 */
class HigherOrLowerGame {
    /** The deck of cards used for the game, containing one of each rank */
    private var deck = Deck().apply { 
        createRankOnlyDeck() // Use only 13 cards (one of each rank)
    }
    /** The player's current card */
    var playerCard: Card? = null

    /** The dealer's current card */
    var dealerCard: Card? = null

    /** The current round number */
    var currentRound: Int = 1

    /** The total number of rounds in the current game */
    var totalRounds: Int = 2

    /** The current win multiplier */
    var multiplier: Int = 2

    /** Whether the player can choose to leave with winnings or go all-in */
    var canLeaveOrAllIn: Boolean = false

    /** Whether the game has ended */
    var gameOver: Boolean = false

    /** Whether the dealer's card is revealed */
    var showDealerCard: Boolean = false

    /** The result message to display in the main game area */
    var lastResult: String? = null

    /** The result message to display in the side menu */
    var sideMenuResult: String? = null

    /** Whether the current round is completed */
    var roundCompleted: Boolean = false

    /**
     * Converts a card's rank to its numerical value.
     * 
     * In Higher or Lower, cards have the following values:
     * - Ace: 14 (highest)
     * - King: 13
     * - Queen: 12
     * - Jack: 11
     * - Number cards: Their face value
     * 
     * @param card The card to evaluate
     * @return The numerical value of the card
     */
    private fun cardValue(card: Card): Int = when (card.rank) {
        "Ace" -> 14
        "King" -> 13
        "Queen" -> 12
        "Jack" -> 11
        else -> card.rank.toInt()
    }

    init {
        dealNewRound()
    }

    /**
     * Prepares a new round of the game.
     * 
     * This private method:
     * 1. Creates a fresh deck with one card of each rank
     * 2. Deals one card to the player and one to the dealer
     * 3. Resets the game state for the new round
     */
    private fun dealNewRound() {
        // Always create a new 13-card deck for each round
        deck = Deck().apply { 
            createRankOnlyDeck() 
        }
        playerCard = deck.cards.removeFirst()
        dealerCard = deck.cards.removeFirst()
        showDealerCard = false
        lastResult = null
        sideMenuResult = null
        roundCompleted = false
    }

    /**
     * Processes the player's guess about whether the dealer's card is higher or lower.
     * 
     * This method:
     * 1. Reveals the dealer's card
     * 2. Compares the player's and dealer's cards
     * 3. Determines if the player's guess was correct
     * 4. Updates the game state based on the outcome
     * 
     * @param higher Whether the player guessed that the dealer's card is higher (true) or lower (false)
     * @param deckStyle The visual style of the deck to use for card images
     * @return The updated state of the game after the guess
     */
    fun guess(higher: Boolean, deckStyle: String): HigherOrLowerGameState {
        if (gameOver || playerCard == null || dealerCard == null) return getState(deckStyle)
        showDealerCard = true
        val playerValue = cardValue(playerCard!!)
        val dealerValue = cardValue(dealerCard!!)
        val win = (higher && dealerValue > playerValue) || (!higher && dealerValue < playerValue)
        lastResult = if (win) "You win!" else "Game over"
        sideMenuResult = if (win) "You win!" else "You lose!"

        if (win) {
            if (currentRound == totalRounds) {
                canLeaveOrAllIn = true
                // Prepare a new deck for the next game
                deck = Deck().apply { 
                    createRankOnlyDeck() 
                }
                return getState(deckStyle)
            } else {
                // Mark the round as completed but don't deal a new round yet
                // This allows the player to see the result before moving to the next round
                roundCompleted = true
                currentRound++
            }
        } else {
            gameOver = true
            // Prepare a new deck for the next game
            deck = Deck().apply { 
                createRankOnlyDeck() 
            }
        }
        return getState(deckStyle)
    }

    /**
     * Advances to the next round of the game.
     * 
     * This method:
     * 1. Checks if the game is over
     * 2. Deals a new round if the current round is completed
     * 3. Returns the updated game state
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The state of the game for the next round
     */
    fun nextRound(deckStyle: String): HigherOrLowerGameState {
        if (gameOver) return getState(deckStyle)
        // Only deal a new round if the current round is completed
        // This ensures the player can see the result before moving to the next round
        if (roundCompleted) {
            dealNewRound()
        }
        return getState(deckStyle)
    }

    /**
     * Processes the player's decision to go "all in" for higher stakes.
     * 
     * When a player chooses to go all in:
     * 1. The total number of rounds increases
     * 2. The multiplier doubles
     * 3. The game resets for a new set of rounds
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The updated state of the game after going all in
     */
    fun allIn(deckStyle: String): HigherOrLowerGameState {
        if (!canLeaveOrAllIn) return getState(deckStyle)
        totalRounds += 1
        multiplier *= 2
        canLeaveOrAllIn = false
        currentRound = 1
        roundCompleted = false
        dealNewRound()
        return getState(deckStyle)
    }

    /**
     * Processes the player's decision to leave the game with current winnings.
     * 
     * This method:
     * 1. Ends the current game
     * 2. Prepares a fresh deck for the next game
     * 3. Returns the final game state
     * 
     * @param deckStyle The visual style of the deck to use for card images
     * @return The final state of the game after leaving
     */
    fun leave(deckStyle: String): HigherOrLowerGameState {
        gameOver = true
        roundCompleted = false
        // Prepare a new deck for the next game
        deck = Deck().apply { 
            createRankOnlyDeck() 
        }
        return getState(deckStyle)
    }

    fun reset(deckStyle: String): HigherOrLowerGameState {
        deck = Deck().apply { 
            createRankOnlyDeck() // Use only 13 cards (one of each rank)
        }
        currentRound = 1
        totalRounds = 3
        multiplier = 2
        canLeaveOrAllIn = false
        gameOver = false
        roundCompleted = false
        dealNewRound()
        return getState(deckStyle)
    }

    fun getState(deckStyle: String): HigherOrLowerGameState =
        HigherOrLowerGameState(
            playerCard = playerCard?.toCardWithImage(deckStyle),
            dealerCard = if (showDealerCard) dealerCard?.toCardWithImage(deckStyle) else null,
            showDealerCard = showDealerCard,
            result = lastResult,
            sideMenuResult = sideMenuResult,
            currentRound = currentRound,
            totalRounds = totalRounds,
            multiplier = multiplier,
            canLeaveOrAllIn = canLeaveOrAllIn,
            gameOver = gameOver,
            roundCompleted = roundCompleted
        )
}
