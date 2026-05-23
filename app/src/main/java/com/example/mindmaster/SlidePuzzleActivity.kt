package com.example.mindmaster

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import java.util.Random

class SlidePuzzleActivity : AppCompatActivity() {

    private lateinit var difficultySelector: View
    private lateinit var gameContainer: View
    private lateinit var gameGrid: GridLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    private lateinit var btnReset: Button
    private lateinit var tvMoves: TextView
    private lateinit var tvBest: TextView

    private var tiles = mutableListOf<Tile>()
    private var tileViews = mutableListOf<TextView>()
    private var gridSize = 3 // Default 3x3
    private var emptyTilePos = 0

    private var currentMoves = 0
    private var bestScore = Int.MAX_VALUE
    private lateinit var sharedPrefs: SharedPreferences
    private var currentDifficultyKey = "slide_best_3"

    // Data class to represent a tile
    data class Tile(val originalIndex: Int, var currentIndex: Int)

    enum class Difficulty(val size: Int, val key: String) {
        EASY(3, "slide_best_3"),
        MEDIUM(4, "slide_best_4"),
        HARD(5, "slide_best_5")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide_puzzle)

        sharedPrefs = getSharedPreferences("SlidePuzzlePrefs", Context.MODE_PRIVATE)

        difficultySelector = findViewById(R.id.difficultySelector)
        gameContainer = findViewById(R.id.gameContainer)
        gameGrid = findViewById(R.id.gameGrid)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)
        btnReset = findViewById(R.id.btnReset)
        tvMoves = findViewById(R.id.tvMoves)
        tvBest = findViewById(R.id.tvBest)

        btnEasy.setOnClickListener { loadGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { loadGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { loadGame(Difficulty.HARD) }
        btnReset.setOnClickListener { resetGame() }
    }

    private fun loadGame(difficulty: Difficulty) {
        gridSize = difficulty.size
        currentDifficultyKey = difficulty.key

        difficultySelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        loadBestScore()
        resetGame()
    }

    private fun resetGame() {
        currentMoves = 0
        updateMoves(0)
        gameGrid.removeAllViews()
        tiles.clear()
        tileViews.clear()

        gameGrid.columnCount = gridSize
        gameGrid.rowCount = gridSize

        // Create a solved puzzle
        val totalTiles = gridSize * gridSize
        for (i in 0 until totalTiles) {
            tiles.add(Tile(i, i))
        }
        emptyTilePos = totalTiles - 1 // Empty tile is the last one

        // Shuffle it by making 100 valid random moves
        shufflePuzzle()

        // Create and add views
        for (i in 0 until totalTiles) {
            val tileView = LayoutInflater.from(this)
                .inflate(R.layout.list_item_tile, gameGrid, false) as TextView

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            tileView.layoutParams = params

            tileView.tag = i // Store its *current position* in the tag
            tileView.setOnClickListener(::onTileClicked)

            gameGrid.addView(tileView)
            tileViews.add(tileView)
        }

        // Update the UI
        updateGridUI()
    }

    private fun shufflePuzzle() {
        val random = Random()
        for (i in 0..100) { // Make 100 random moves
            val possibleMoves = getPossibleMoves(emptyTilePos)
            if (possibleMoves.isNotEmpty()) {
                val movePos = possibleMoves[random.nextInt(possibleMoves.size)]
                swapTiles(movePos, emptyTilePos)
            }
        }
    }

    private fun getPossibleMoves(pos: Int): List<Int> {
        val moves = mutableListOf<Int>()
        val row = pos / gridSize
        val col = pos % gridSize

        if (row > 0) moves.add(pos - gridSize) // Up
        if (row < gridSize - 1) moves.add(pos + gridSize) // Down
        if (col > 0) moves.add(pos - 1) // Left
        if (col < gridSize - 1) moves.add(pos + 1) // Right
        return moves
    }

    private fun updateGridUI() {
        for (i in 0 until tiles.size) {
            val tile = tiles[i]
            val tileView = tileViews[i]
            tileView.tag = i // Make sure tag is updated to current position

            if (tile.originalIndex == tiles.size - 1) {
                // This is the empty tile
                tileView.text = ""
                tileView.setBackgroundResource(R.drawable.empty_tile_background)
                tileView.isClickable = false
            } else {
                // This is a numbered tile
                tileView.text = (tile.originalIndex + 1).toString()
                tileView.setBackgroundResource(R.drawable.tile_background)
                tileView.isClickable = true
            }
        }
    }

    private fun onTileClicked(view: View) {
        val clickedPos = view.tag as Int

        // Check if this tile is adjacent to the empty tile
        val emptyRow = emptyTilePos / gridSize
        val emptyCol = emptyTilePos % gridSize
        val clickedRow = clickedPos / gridSize
        val clickedCol = clickedPos % gridSize

        val isAdjacent = (emptyRow == clickedRow && Math.abs(emptyCol - clickedCol) == 1) ||
                (emptyCol == clickedCol && Math.abs(emptyRow - clickedRow) == 1)

        if (isAdjacent) {
            swapTiles(clickedPos, emptyTilePos)
            updateMoves(1)
            updateGridUI()

            if (checkWinCondition()) {
                showGameWonDialog()
            }
        }
    }

    private fun swapTiles(pos1: Int, pos2: Int) {
        // Swap in the data list
        val temp = tiles[pos1]
        tiles[pos1] = tiles[pos2]
        tiles[pos2] = temp

        // Update the current index in the tile objects
        tiles[pos1].currentIndex = pos1
        tiles[pos2].currentIndex = pos2

        // Update the empty tile position
        if (tiles[pos1].originalIndex == tiles.size - 1) {
            emptyTilePos = pos1
        } else if (tiles[pos2].originalIndex == tiles.size - 1) {
            emptyTilePos = pos2
        }
    }

    private fun checkWinCondition(): Boolean {
        for (i in 0 until tiles.size) {
            if (tiles[i].originalIndex != i) {
                return false
            }
        }
        return true
    }

    private fun updateMoves(add: Int) {
        currentMoves += add
        tvMoves.text = "Moves: $currentMoves"
    }

    private fun loadBestScore() {
        bestScore = sharedPrefs.getInt(currentDifficultyKey, Int.MAX_VALUE)
        updateBestScoreUI()
    }

    private fun updateBestScoreUI() {
        if (bestScore == Int.MAX_VALUE) {
            tvBest.text = "Best: -"
        } else {
            tvBest.text = "Best: $bestScore"
        }
    }

    private fun checkAndSaveBestScore() {
        if (currentMoves < bestScore) {
            bestScore = currentMoves
            sharedPrefs.edit().putInt(currentDifficultyKey, bestScore).apply()
            updateBestScoreUI()
        }
    }

    private fun showGameWonDialog() {
        checkAndSaveBestScore()
        val message = "You won in $currentMoves moves!\n" +
                if (currentMoves == bestScore) "That's a new best score!" else "Best: $bestScore"

        AlertDialog.Builder(this)
            .setTitle("You Won!")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                resetGame()
            }
            .setNeutralButton("Change Difficulty") { _, _ ->
                gameContainer.visibility = View.GONE
                difficultySelector.visibility = View.VISIBLE
            }
            .setCancelable(false)
            .show()
    }
}
