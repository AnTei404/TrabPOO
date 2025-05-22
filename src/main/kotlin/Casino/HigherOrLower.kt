package trab.casino

data class HigherOrLowerGameState(
    val playerCard: Map<String, String>?,
    val dealerCard: Map<String, String>?, // Hidden until after guess
    val showDealerCard: Boolean = false,
    val result: String? = null,
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val multiplier: Int = 1,
    val canLeaveOrAllIn: Boolean = false,
    val gameOver: Boolean = false
)

class HigherOrLowerGame {
    private var deck = Deck().apply { shuffle() }
    var playerCard: Card? = null
    var dealerCard: Card? = null
    var currentRound: Int = 1
    var totalRounds: Int = 2
    var multiplier: Int = 2
    var canLeaveOrAllIn: Boolean = false
    var gameOver: Boolean = false
    var showDealerCard: Boolean = false
    var lastResult: String? = null

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
        if (deck.cards.size < 52) deck = Deck().apply { shuffle() }
        playerCard = deck.cards.removeFirst()
        dealerCard = deck.cards.removeFirst()
        showDealerCard = false
        lastResult = null
    }

    fun guess(higher: Boolean, deckStyle: String): HigherOrLowerGameState {
        if (gameOver || playerCard == null || dealerCard == null) return getState(deckStyle)
        showDealerCard = true
        val playerValue = cardValue(playerCard!!)
        val dealerValue = cardValue(dealerCard!!)
        val win = (higher && dealerValue > playerValue) || (!higher && dealerValue < playerValue)
        lastResult = if (win) "You win!" else "You lose!"

        if (win) {
            if (currentRound == totalRounds) {
                canLeaveOrAllIn = true
                return getState(deckStyle)
            } else {
                currentRound++
            }
        } else {
            gameOver = true
        }
        return getState(deckStyle)
    }

    fun nextRound(deckStyle: String): HigherOrLowerGameState {
        if (gameOver) return getState(deckStyle)
        dealNewRound()
        return getState(deckStyle)
    }

    fun allIn(deckStyle: String): HigherOrLowerGameState {
        if (!canLeaveOrAllIn) return getState(deckStyle)
        totalRounds += 1
        multiplier *= 2
        canLeaveOrAllIn = false
        currentRound = 1
        dealNewRound()
        return getState(deckStyle)
    }

    fun leave(deckStyle: String): HigherOrLowerGameState {
        gameOver = true
        return getState(deckStyle)
    }

    fun reset(deckStyle: String): HigherOrLowerGameState {
        deck = Deck().apply { shuffle() }
        currentRound = 1
        totalRounds = 3
        multiplier = 2
        canLeaveOrAllIn = false
        gameOver = false
        dealNewRound()
        return getState(deckStyle)
    }

    fun getState(deckStyle: String): HigherOrLowerGameState =
        HigherOrLowerGameState(
            playerCard = playerCard?.toCardWithImage(deckStyle),
            dealerCard = if (showDealerCard) dealerCard?.toCardWithImage(deckStyle) else null,
            showDealerCard = showDealerCard,
            result = lastResult,
            currentRound = currentRound,
            totalRounds = totalRounds,
            multiplier = multiplier,
            canLeaveOrAllIn = canLeaveOrAllIn,
            gameOver = gameOver
        )
}