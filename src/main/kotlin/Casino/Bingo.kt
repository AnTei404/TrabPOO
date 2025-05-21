package trab.casino

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class BingoCell(val number: Int, var marked: Boolean = false)

@Serializable
data class BingoCard(val grid: List<List<BingoCell>>) {
    fun mark(number: Int) {
        for (row in grid) {
            for (cell in row) {
                if (cell.number == number) cell.marked = true
            }
        }
    }
    fun hasBingo(): Boolean {
        return grid.all { row -> row.all { it.marked } }
    }
}

class BingoGame(
    val numHouseCards: Int = 5 // Now 5 house cards
) {
    val userCard: BingoCard = generateCard()
    val houseCards: List<BingoCard> = List(numHouseCards) { generateCard() }
    val drawnNumbers = mutableListOf<Int>()
    val allNumbers = (1..75).toMutableList().apply { shuffle() }
    var lastNumber: Int? = null

    fun nextNumber(): Int? {
        if (allNumbers.isEmpty()) return null
        val number = allNumbers.removeAt(0)
        drawnNumbers.add(number)
        lastNumber = number
        userCard.mark(number)
        houseCards.forEach { it.mark(number) }
        return number
    }

    fun userHasBingo() = userCard.hasBingo()
    fun houseWinners(): List<Int> = houseCards.mapIndexedNotNull { idx, card -> if (card.hasBingo()) idx + 1 else null }

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