package trab.casino

import kotlinx.serialization.Serializable

@Serializable
data class RideTheBusGameState(
    val cards: List<Map<String, String>>,
    val revealed: List<Boolean>,
    val currentRound: Int,
    val multiplier: Int,
    val gameOver: Boolean,
    val result: String? = null,
    val canLeave: Boolean = false
)

class RideTheBusGame(deckStyle: String) {
    private var deck = Deck().apply { 
        createRankOnlyDeck()
    }
    private var cards = List(4) { deck.cards.removeFirst() }
    private var revealed = MutableList(4) { false }
    private var round = 0
    private var multiplier = 1
    private var gameOver = false
    private var result: String? = null

    fun getState(deckStyle: String) = RideTheBusGameState(
        cards = cards.mapIndexed { i, c -> if (revealed[i]) c.toCardWithImage(deckStyle) else mapOf("imagePath" to "/Baralhos/CardBack.png") },
        revealed = revealed.toList(),
        currentRound = round,
        multiplier = multiplier,
        gameOver = gameOver,
        result = result,
        canLeave = round > 0 && !gameOver
    )

    fun guess(choice: String, deckStyle: String): RideTheBusGameState {
        if (gameOver) {
            return reset(deckStyle)
        }

        val card = cards[round]
        val prevCard = if (round > 0) cards[round - 1] else null
        val correct = when (round) {
            0 -> (choice == "red" && (card.suit == "Hearts" || card.suit == "Diamonds")) || (choice == "black" && (card.suit == "Clubs" || card.suit == "Spades"))
            1 -> prevCard != null && ((choice == "higher" && cardValue(card) > cardValue(prevCard)) || (choice == "lower" && cardValue(card) < cardValue(prevCard)))
            2 -> prevCard != null && ((choice == "inside" && cardValue(card) in (minOf(cardValue(cards[0]), cardValue(cards[1])) + 1 until maxOf(cardValue(cards[0]), cardValue(cards[1]))) || (choice == "outside" && (
                    cardValue(card) < minOf(cardValue(cards[0]), cardValue(cards[1])) || cardValue(card) > maxOf(cardValue(cards[0]), cardValue(cards[1]))))))
            3 -> choice.equals(card.suit, ignoreCase = true)
            else -> false
        }
        revealed[round] = true
        if (correct) {
            multiplier = when (round) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 20
                else -> multiplier
            }
            round++
            if (round == 4) {
                gameOver = true
                result = "You won! Multiplier: $multiplier"
                deck = Deck().apply { 
                    createRankOnlyDeck() 
                }
            }
        } else {
            gameOver = true
            result = "You lost! Final multiplier: $multiplier"
            deck = Deck().apply { 
                createRankOnlyDeck() 
            }
        }
        return getState(deckStyle)
    }

    fun leave(deckStyle: String): RideTheBusGameState {
        gameOver = true
        result = "You left with multiplier: $multiplier"
        deck = Deck().apply { 
            createRankOnlyDeck() 
        }
        return getState(deckStyle)
    }

    fun reset(deckStyle: String): RideTheBusGameState {

        deck = Deck().apply { 
            createRankOnlyDeck() // Use only 13 cards (one of each rank)
        }
        cards = List(4) { deck.cards.removeFirst() }
        revealed = MutableList(4) { false }
        round = 0
        multiplier = 1
        gameOver = false
        result = null
        return getState(deckStyle)
    }

    private fun cardValue(card: Card): Int = when (card.rank) {
        "Ace" -> 14
        "King" -> 13
        "Queen" -> 12
        "Jack" -> 11
        else -> card.rank.toInt()
    }
}
