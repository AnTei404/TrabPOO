package trab.casino

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Represents the current state of a Bingo game.
 * 
 * This serializable data class contains all information needed to render
 * the game state in the UI and determine the outcome of the game.
 * 
 * @property userCard The player's Bingo card
 * @property houseCards The house (computer) Bingo cards
 * @property drawnNumbers List of all numbers that have been drawn so far
 * @property lastNumber The most recently drawn number, or null if no number has been drawn
 * @property userHasBingo Whether the player has achieved Bingo
 * @property houseWinners List of indices of house cards that have achieved Bingo
 * @property tie Whether the game ended in a tie
 */
@Serializable
data class BingoGameState(
    val userCard: BingoCard,
    val houseCards: List<BingoCard>,
    val drawnNumbers: List<Int>,
    val lastNumber: Int?,
    val userHasBingo: Boolean,
    val houseWinners: List<Int>,
    val tie: Boolean = false
)

/**
 * Represents a single cell on a Bingo card.
 * 
 * Each cell contains a number and a flag indicating whether it has been marked.
 * 
 * @property number The number displayed in this cell (1-75)
 * @property marked Whether this cell has been marked (true) or not (false)
 */
@Serializable
data class BingoCell(val number: Int, var marked: Boolean = false)

/**
 * Represents a Bingo card with a grid of cells.
 * 
 * A standard Bingo card is a 5x5 grid of numbers, with the center cell
 * typically marked as a "free" space.
 * 
 * @property grid A 2D list of BingoCell objects representing the card layout
 */
@Serializable
data class BingoCard(val grid: List<List<BingoCell>>) {
    /**
     * Marks all cells on the card that match the given number.
     * 
     * @param number The number to mark on the card
     */
    fun mark(number: Int) {
        for (row in grid) {
            for (cell in row) {
                if (cell.number == number) cell.marked = true
            }
        }
    }

    /**
     * Checks if this card has achieved Bingo.
     * 
     * In this implementation, Bingo is achieved when all cells on the card are marked.
     * 
     * @return true if Bingo has been achieved, false otherwise
     */
    fun hasBingo(): Boolean {
        return grid.all { row -> row.all { it.marked } }
    }
}

/**
 * Implements the game logic for Bingo.
 * 
 * This class manages:
 * - The player's Bingo card
 * - The house (computer) Bingo cards
 * - Drawing numbers
 * - Tracking game progress
 * - Determining winners
 * 
 * @property numHouseCards The number of Bingo cards the house (computer) plays with
 */
class BingoGame(
    val numHouseCards: Int = 2 // Number of house cards
) {
    /** The player's Bingo card */
    val userCard: BingoCard = generateCard()

    /** The house (computer) Bingo cards */
    val houseCards: List<BingoCard> = List(numHouseCards) { generateCard() }

    /** List of all numbers that have been drawn so far */
    val drawnNumbers = mutableListOf<Int>()

    /** Pool of all possible numbers (1-75) that can be drawn */
    val allNumbers = (1..75).toMutableList().apply { shuffle() }

    /** The most recently drawn number, or null if no number has been drawn */
    var lastNumber: Int? = null

    /**
     * Draws the next number in the Bingo game.
     * 
     * This method:
     * 1. Draws a random number from the pool of available numbers
     * 2. Adds it to the list of drawn numbers
     * 3. Updates the lastNumber property
     * 4. Marks the number on all cards (player and house)
     * 
     * @return The drawn number, or null if all numbers have been drawn
     */
    fun nextNumber(): Int? {
        if (allNumbers.isEmpty()) return null
        val number = allNumbers.removeAt(0)
        drawnNumbers.add(number)
        lastNumber = number
        userCard.mark(number)
        houseCards.forEach { it.mark(number) }
        return number
    }

    /**
     * Checks if the player has achieved Bingo.
     * 
     * @return true if the player has Bingo, false otherwise
     */
    fun userHasBingo() = userCard.hasBingo()

    /**
     * Identifies which house cards have achieved Bingo.
     * 
     * @return A list of indices (1-based) of house cards that have Bingo
     */
    fun houseWinners(): List<Int> = houseCards.mapIndexedNotNull { idx, card -> if (card.hasBingo()) idx + 1 else null }

    /**
     * Generates a random Bingo card.
     * 
     * Creates a 5x5 grid of numbers following standard Bingo rules:
     * - Each column contains numbers from a specific range
     * - The center cell is marked as a free space
     * 
     * @return A new randomly generated BingoCard
     */
    private fun generateCard(): BingoCard {
        val columns = listOf(
            (1..15).shuffled().take(5),
            (16..30).shuffled().take(5),
            (31..45).shuffled().take(5),
            (46..60).shuffled().take(5),
            (61..75).shuffled().take(5)
        )
        val grid = List(5) { row ->
            List(5) { col ->
                val number = columns[col][row]
                BingoCell(number, marked = (row == 2 && col == 2)) // Free space in center
            }
        }
        return BingoCard(grid)
    }
}
