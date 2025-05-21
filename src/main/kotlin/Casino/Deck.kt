package trab.casino

import kotlinx.serialization.Serializable

@Serializable
data class Card(val rank: String, val suit: String)

class Deck {
    private val suits = listOf("Hearts", "Diamonds", "Clubs", "Spades")
    private val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace")
    var cards: MutableList<Card> = suits.flatMap { suit -> ranks.map { rank -> Card(rank, suit) } }.toMutableList()

    fun shuffle() {
        cards = cards.shuffled().toMutableList()
    }
}