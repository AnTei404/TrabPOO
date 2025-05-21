package trab.casino

import kotlin.random.Random

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

    data class SpinResult(
        val grid: List<List<String>>,
        val winLines: List<String>,
        val payout: Int
    )

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