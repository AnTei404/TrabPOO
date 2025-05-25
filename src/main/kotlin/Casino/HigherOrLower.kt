package trab.casino

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

class HigherOrLowerGame {
    private var deck = Deck().apply { 
        createRankOnlyDeck() // Use only 13 cards (one of each rank)
    }
    var playerCard: Card? = null
    var dealerCard: Card? = null
    var currentRound: Int = 1
    var totalRounds: Int = 2
    var multiplier: Int = 2
    var canLeaveOrAllIn: Boolean = false
    var gameOver: Boolean = false
    var showDealerCard: Boolean = false
    var lastResult: String? = null
    var sideMenuResult: String? = null
    var roundCompleted: Boolean = false

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

    fun nextRound(deckStyle: String): HigherOrLowerGameState {
        if (gameOver) return getState(deckStyle)
        // Only deal a new round if the current round is completed
        // This ensures the player can see the result before moving to the next round
        if (roundCompleted) {
            dealNewRound()
        }
        return getState(deckStyle)
    }

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
