package trab.casino

import kotlin.random.Random

data class MinesGameState(
    val grid: List<List<MinesCell>>,
    val gameOver: Boolean = false,
    val mineRevealed: Boolean = false,
    val minePosition: Pair<Int, Int>? = null,
    val squaresRevealed: Int = 0,
    val payout: Int = 0,
    val payoutMultiplier: Double = 0.0
)

data class MinesCell(
    val row: Int,
    val col: Int,
    var revealed: Boolean = false,
    var hasMine: Boolean = false
)

class Mines(private val betAmount: Int = 100) {
    private val gridSize = 5
    private var grid: List<List<MinesCell>> = initializeGrid()
    private var minePosition: Pair<Int, Int> = placeMine()
    private var gameOver: Boolean = false
    private var mineRevealed: Boolean = false
    private var squaresRevealed: Int = 0

    private val payoutMultipliers = mapOf(
        0 to -1.0,
        1 to -0.7,
        2 to -0.4,
        3 to -0.2,
        4 to 1.0,
        5 to 1.2,
        6 to 1.4,
        7 to 1.7,
        8 to 2.0,
        9 to 2.2,
        10 to 2.5,
        11 to 2.8,
        12 to 3.2,
        13 to 3.6,
        14 to 4.0,
        15 to 4.5,
        16 to 5.0,
        17 to 5.5,
        18 to 6.0,
        19 to 7.0,
        20 to 8.0,
        21 to 9.0,
        22 to 10.0,
        23 to 11.0,
        24 to 25.0
    )

    private fun initializeGrid(): List<List<MinesCell>> {
        return List(gridSize) { row ->
            List(gridSize) { col ->
                MinesCell(row, col)
            }
        }
    }

    private fun placeMine(): Pair<Int, Int> {
        val row = Random.nextInt(0, gridSize)
        val col = Random.nextInt(0, gridSize)
        grid[row][col].hasMine = true
        return Pair(row, col)
    }

    fun revealSquare(row: Int, col: Int): MinesGameState {
        if (gameOver || row < 0 || row >= gridSize || col < 0 || col >= gridSize || grid[row][col].revealed) {
            return getState()
        }
        grid[row][col].revealed = true
        if (grid[row][col].hasMine) {
            gameOver = true
            mineRevealed = true
            return getState()
        }
        squaresRevealed++
        if (squaresRevealed == gridSize * gridSize - 1) {
            gameOver = true
            return getState()
        }
        return getState()
    }

    fun endGame(): MinesGameState {
        gameOver = true
        return getState()
    }

    fun getState(): MinesGameState {
        val currentMultiplier = payoutMultipliers[squaresRevealed] ?: 0.0
        val payout = if (mineRevealed) 0 else (currentMultiplier * betAmount).toInt()

        return MinesGameState(
            grid = grid,
            gameOver = gameOver,
            mineRevealed = mineRevealed,
            minePosition = if (gameOver) minePosition else null,
            squaresRevealed = squaresRevealed,
            payout = payout,
            payoutMultiplier = currentMultiplier
        )
    }

}
