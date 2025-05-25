package trab.casino

import kotlinx.serialization.Serializable

/**
 * Represents a playing card with a rank and suit.
 * 
 * This class is serializable to allow for easy conversion to JSON
 * when sending card data to the client.
 * 
 * @property rank The rank of the card (2-10, Jack, Queen, King, Ace)
 * @property suit The suit of the card (Hearts, Diamonds, Clubs, Spades)
 */
@Serializable
data class Card(val rank: String, val suit: String)

/**
 * Represents a deck of playing cards.
 * 
 * This class manages a collection of Card objects and provides
 * methods for common deck operations like shuffling.
 */
class Deck {
    /** Standard card suits */
    private val suits = listOf("Hearts", "Diamonds", "Clubs", "Spades")

    /** Standard card ranks */
    private val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace")

    /**
     * The collection of cards in the deck.
     * Initialized with a standard 52-card deck (13 ranks × 4 suits).
     */
    var cards: MutableList<Card> = suits.flatMap { suit -> ranks.map { rank -> Card(rank, suit) } }.toMutableList()

    /**
     * Shuffles the cards in the deck.
     * 
     * This randomizes the order of cards to ensure fair gameplay.
     */
    fun shuffle() {
        cards = cards.shuffled().toMutableList()
    }

    /**
     * Creates a deck with only 13 cards (one of each rank).
     * 
     * This is useful for games that only care about card ranks,
     * not suits. Each rank is assigned a random suit.
     */
    fun createRankOnlyDeck() {
        // Choose a random suit for each rank
        cards = ranks.map { rank -> 
            Card(rank, suits.random())
        }.toMutableList()
        shuffle()
    }
}
