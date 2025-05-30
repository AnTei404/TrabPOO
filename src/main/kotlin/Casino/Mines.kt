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

/**
 * Implementation of the Mines game.
 * 
 * Game rules:
 * - A 5x5 grid is initialized with one mine placed at a random position.
 * - The mine position is fixed for the duration of a game and only changes when the game is reset.
 * - Players reveal squares one by one, trying to avoid the mine.
 * - If a mine is revealed, the game ends and the player loses their bet.
 * - The payout increases as more safe squares are revealed.
 * - The game ends when either the mine is revealed or the player chooses to cash out.
 *
 * @property betAmount The amount bet by the player
 */
class Mines(private val betAmount: Int = 100) {
    private val gridSize = 5
    private var grid: List<List<MinesCell>> = initializeGrid()
    private var minePosition: Pair<Int, Int> = placeMine()
    private var gameOver: Boolean = false
    private var mineRevealed: Boolean = false
    private var squaresRevealed: Int = 0

    // Payout multipliers based on number of squares revealed
    private val payoutMultipliers = mapOf(
        0 to -1.0,  // Start with negative payout
        1 to -0.7,
        2 to -0.4,
        3 to -0.2,
        4 to 1.0,   // Break even at 4 squares
        5 to 1.2,
        6 to 1.4,
        7 to 1.7,
        8 to 2.0,
        9 to 2.2,
        10 to 2.5,  // 2.5x at 10 squares
        11 to 2.8,
        12 to 3.2,
        13 to 3.6,
        14 to 4.0,
        15 to 4.5,  // 4.5x at 15 squares
        16 to 5.0,
        17 to 5.5,
        18 to 6.0,
        19 to 7.0,
        20 to 8.0,  // 8.0x at 20 squares
        21 to 9.0,  // 9.0x at 21 squares
        22 to 10.0, // 10.0x at 22 squares
        23 to 11.0, // 11.0x at 23 squares
        24 to 25.0  // 12.0x at 24 squares (all except the mine)
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

        // Check if only the mine square is left
        if (squaresRevealed == gridSize * gridSize - 1) {
            // Only one square left, and it must be the mine
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
