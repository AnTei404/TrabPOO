package trab.casino

import kotlin.random.Random

/**
 * Implements a slot machine game with a 3x3 grid.
 * 
 * This class handles the game logic for a slot machine, including:
 * - Random symbol generation with weighted probabilities
 * - Win line detection (horizontal, vertical, and diagonal)
 * - Payout calculation based on symbol values and bet amount
 */
class Slots {
    private val weightedSymbols = listOf(
        "Lemon", "Lemon", "Lemon", "Lemon", "Lemon", "Lemon",
        "Orange", "Orange", "Orange", "Orange", "Orange",
        "Cherry", "Cherry", "Cherry", "Cherry",
        "Mine", "Mine","Mine",
        "Bell", "Bell",
        "Seven"
    )

    private val payouts = mapOf(
        "Seven" to 10,
        "Bell" to 5,
        "Cherry" to 3,
        "Orange" to 2,
        "Lemon" to 1,
        "Mine" to 0
    )

    /**
     * Represents the result of a slot machine spin.
     * 
     * @property grid A 3x3 grid of symbols representing the slot machine display
     * @property winLines List of winning lines (e.g., "horizontal-0", "vertical-1", "diagonal-main")
     * @property payout The total payout amount for this spin
     */
    data class SpinResult(
        val grid: List<List<String>>,
        val winLines: List<String>,
        val payout: Int
    )

    /**
     * Performs a spin on the slot machine.
     * 
     * This method:
     * 1. Generates a random 3x3 grid of symbols
     * 2. Checks for winning lines (horizontal, vertical, diagonal)
     * 3. Calculates the total payout based on the bet amount and symbol values
     * 
     * @param bet The amount of chips bet on this spin
     * @return A SpinResult containing the grid, winning lines, and payout
     */
    fun spin(bet: Int): SpinResult {
        val grid = List(3) { List(3) { weightedSymbols.random() } }
        val winLines = mutableListOf<String>()
        var payout = 0

        fun linePayout(symbol: String) = payouts[symbol] ?: 0

        for (i in 0..2) {
            val row = grid[i]
            if (row.distinct().size == 1) {
                winLines.add("horizontal-$i")
                payout += bet * linePayout(row[0])
            }
        }
        for (j in 0..2) {
            val col = listOf(grid[0][j], grid[1][j], grid[2][j])
            if (col.distinct().size == 1) {
                winLines.add("vertical-$j")
                payout += bet * linePayout(col[0])
            }
        }
        val mainDiag = (0..2).map { grid[it][it] }
        if (mainDiag.distinct().size == 1) {
            winLines.add("diagonal-main")
            payout += bet * linePayout(mainDiag[0])
        }
        val antiDiag = (0..2).map { grid[it][2 - it] }
        if (antiDiag.distinct().size == 1) {
            winLines.add("diagonal-anti")
            payout += bet * linePayout(antiDiag[0])
        }

        return SpinResult(grid, winLines, payout)
    }
}
