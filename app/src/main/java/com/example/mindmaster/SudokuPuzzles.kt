package com.example.mindmaster

// A simple object to hold pre-defined puzzles
// 0 represents an empty cell
object SudokuPuzzles {

    // Pair<Puzzle, Solution>
    private val easy = Pair(
        arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        ),
        arrayOf(
            intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
            intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
            intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
            intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
            intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
            intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
            intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
        )
    )

    // --- Using this as the "Normal" puzzle ---
    private val mediumPuzzle = Pair(
        arrayOf(
            intArrayOf(8, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 3, 6, 0, 0, 0, 0, 0),
            intArrayOf(0, 7, 0, 0, 9, 0, 2, 0, 0),
            intArrayOf(0, 5, 0, 0, 0, 7, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 4, 5, 7, 0, 0),
            intArrayOf(0, 0, 0, 1, 0, 0, 0, 3, 0),
            intArrayOf(0, 0, 1, 0, 0, 0, 0, 6, 8),
            intArrayOf(0, 0, 8, 5, 0, 0, 0, 1, 0),
            intArrayOf(0, 9, 0, 0, 0, 0, 4, 0, 0)
        ),
        arrayOf(
            intArrayOf(8, 1, 2, 7, 5, 3, 6, 4, 9),
            intArrayOf(9, 4, 3, 6, 8, 2, 1, 7, 5),
            intArrayOf(6, 7, 5, 4, 9, 1, 2, 8, 3),
            intArrayOf(1, 5, 4, 2, 3, 7, 8, 9, 6),
            intArrayOf(3, 6, 9, 8, 4, 5, 7, 2, 1),
            intArrayOf(2, 8, 7, 1, 6, 9, 5, 3, 4),
            intArrayOf(5, 2, 1, 9, 7, 4, 3, 6, 8),
            intArrayOf(4, 3, 8, 5, 2, 6, 9, 1, 7),
            intArrayOf(7, 9, 6, 3, 1, 8, 4, 5, 2)
        )
    )

    // --- NEW: Added a new, valid Hard puzzle ---
    private val hardPuzzle = Pair(
        arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 6, 0, 0, 0),
            intArrayOf(0, 5, 9, 0, 0, 0, 0, 0, 8),
            intArrayOf(2, 0, 0, 0, 0, 8, 0, 0, 0),
            intArrayOf(0, 4, 5, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 3, 0, 0, 0, 0, 9, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 7, 0),
            intArrayOf(0, 2, 0, 0, 0, 0, 9, 8, 0),
            intArrayOf(0, 0, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 4, 0, 0, 0, 0, 0)
        ),
        arrayOf(
            intArrayOf(4, 8, 1, 7, 9, 6, 2, 5, 3),
            intArrayOf(3, 5, 9, 2, 4, 1, 7, 6, 8),
            intArrayOf(2, 6, 7, 5, 3, 8, 1, 4, 9),
            intArrayOf(9, 4, 5, 6, 1, 7, 8, 3, 2),
            intArrayOf(7, 1, 3, 8, 2, 5, 6, 9, 4),
            intArrayOf(8, 2, 6, 3, 9, 4, 5, 7, 1),
            intArrayOf(1, 2, 4, 7, 5, 3, 9, 8, 6),
            intArrayOf(5, 9, 8, 1, 7, 2, 3, 4, 6),
            intArrayOf(6, 7, 3, 4, 8, 9, 1, 2, 5)
        )
    )

    // --- REMOVED: The old, broken hard puzzles ---

    // Finding and typing out correct sudoku puzzles is error prone
    // In a real app, you'd load this from a file or generator

    fun getPuzzle(level: Int): Pair<Array<IntArray>, Array<IntArray>> {
        return when (level) {
            1 -> easy
            // --- FIXED: Use the new medium puzzle ---
            2 -> mediumPuzzle
            // --- FIXED: Use the new hard puzzle ---
            3 -> hardPuzzle
            else -> easy
        }
    }
}

