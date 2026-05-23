package com.example.mindmaster

import android.graphics.Color // <-- Make sure this is imported
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.example.mindmaster.TestTubeView // Import the new custom view

class WaterSortActivity : AppCompatActivity() {

    private lateinit var levelSelector: View
    private lateinit var gameContainer: View
    private lateinit var gameGrid: GridLayout
    private lateinit var btnEasy: Button
    private lateinit var btnNormal: Button
    private lateinit var btnHard: Button
    private lateinit var btnReset: Button
    private lateinit var tvMoves: TextView
    private lateinit var tvLevel: TextView // <-- Renamed to tvDifficulty in XML
    private lateinit var tvDifficulty: TextView // <-- NEW: More accurate name

    private var moves = 0
    // --- CHANGED: Renamed for clarity ---
    private var currentDifficulty = 1
    private var currentDifficultyName = "Easy"
    // ------------------------------------
    private var selectedTube: TestTubeView? = null

    // Pre-defined colors
    companion object {
        const val MAX_LEVEL = 4 // This is the max liquid per tube
        val COLOR_RED = Color.rgb(239, 83, 80)
        val COLOR_BLUE = Color.rgb(66, 165, 245)
        val COLOR_GREEN = Color.rgb(102, 187, 106)
        val COLOR_YELLOW = Color.rgb(255, 238, 88)
        val COLOR_ORANGE = Color.rgb(255, 167, 38)
        val COLOR_PURPLE = Color.rgb(171, 71, 188)
    }

    // --- CHANGED: Renamed for clarity ---
    private val difficultyLevels = mapOf(
        1 to listOf( // Easy
            listOf(COLOR_BLUE, COLOR_RED, COLOR_BLUE, COLOR_RED),
            listOf(COLOR_RED, COLOR_BLUE, COLOR_RED, COLOR_BLUE),
            listOf(),
            listOf()
        ),
        2 to listOf( // Normal
            listOf(COLOR_GREEN, COLOR_RED, COLOR_BLUE, COLOR_GREEN),
            listOf(COLOR_BLUE, COLOR_GREEN, COLOR_RED, COLOR_RED),
            listOf(COLOR_GREEN, COLOR_BLUE, COLOR_RED, COLOR_BLUE),
            listOf(),
            listOf()
        ),
        3 to listOf( // Hard
            listOf(COLOR_ORANGE, COLOR_YELLOW, COLOR_PURPLE, COLOR_BLUE),
            listOf(COLOR_ORANGE, COLOR_YELLOW, COLOR_GREEN, COLOR_RED),
            listOf(COLOR_GREEN, COLOR_BLUE, COLOR_PURPLE, COLOR_RED),
            listOf(COLOR_YELLOW, COLOR_GREEN, COLOR_ORANGE, COLOR_RED),
            listOf(COLOR_BLUE, COLOR_PURPLE, COLOR_YELLOW, COLOR_ORANGE),
            listOf(COLOR_RED, COLOR_BLUE, COLOR_PURPLE, COLOR_GREEN),
            listOf(),
            listOf()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_sort)

        levelSelector = findViewById(R.id.levelSelector)
        gameContainer = findViewById(R.id.gameContainer)
        gameGrid = findViewById(R.id.gameGrid)
        btnEasy = findViewById(R.id.btnEasy)
        btnNormal = findViewById(R.id.btnNormal)
        btnHard = findViewById(R.id.btnHard)
        btnReset = findViewById(R.id.btnReset)
        tvMoves = findViewById(R.id.tvMoves)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        // ----------------------------------------------------

        // --- CHANGED: Call loadDifficulty ---
        btnEasy.setOnClickListener { loadDifficulty(1) }
        btnNormal.setOnClickListener { loadDifficulty(2) }
        btnHard.setOnClickListener { loadDifficulty(3) }
        // Reset loads the original, non-shuffled level
        btnReset.setOnClickListener { loadDifficulty(currentDifficulty, shuffle = false) }
        // ------------------------------------
    }

    // --- NEW: Function to generate a shuffled level ---
    private fun generateShuffledLevel(difficulty: Int): List<List<Int>> {
        val baseConfig = difficultyLevels[difficulty]
        if (baseConfig == null) {
            Log.e("WaterSort", "Base config for difficulty $difficulty not found")
            return listOf() // Return empty
        }

        // 1. Get all colors from the base config
        val allColors = baseConfig.flatten().toMutableList()
        allColors.shuffle() // 2. Shuffle them

        // 3. Find out how many tubes were filled and how many were empty
        val emptyTubeCount = baseConfig.count { it.isEmpty() }
        val filledTubeCount = baseConfig.size - emptyTubeCount

        val newConfig = mutableListOf<List<Int>>()
        var colorIndex = 0

        // 4. Re-distribute shuffled colors into the 'filled' tubes
        for (i in 0 until filledTubeCount) {
            val tubeColors = mutableListOf<Int>()
            for (j in 0 until MAX_LEVEL) { // MAX_LEVEL is 4
                if (colorIndex < allColors.size) {
                    tubeColors.add(allColors[colorIndex])
                    colorIndex++
                }
            }
            newConfig.add(tubeColors)
        }

        // 5. Add the correct number of empty tubes back
        for (i in 0 until emptyTubeCount) {
            newConfig.add(listOf())
        }

        // 6. Return the new shuffled configuration
        return newConfig
    }
    // --------------------------------------------------

    // --- CHANGED: Renamed and added shuffle logic ---
    private fun loadDifficulty(difficulty: Int, shuffle: Boolean = false) {
        currentDifficulty = difficulty
        currentDifficultyName = when(difficulty) {
            1 -> "Easy"
            2 -> "Normal"
            3 -> "Hard"
            else -> "Unknown"
        }

        moves = 0
        tvMoves.text = "Moves: 0"
        tvDifficulty.text = "Difficulty: $currentDifficultyName" // --- CHANGED ---
        selectedTube = null

        // --- CHANGED: Get either a shuffled or original level ---
        val levelData = if (shuffle) {
            generateShuffledLevel(difficulty)
        } else {
            difficultyLevels[difficulty]
        }
        // -----------------------------------------------------

        if (levelData == null || levelData.isEmpty()) {
            Log.e("WaterSort", "Level $difficulty data not found or is empty")
            return
        }

        levelSelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE
        gameGrid.removeAllViews()
        gameGrid.columnCount = 4 // Let's set a fixed column count for wrapping

        for (colors in levelData) {
            val tube = TestTubeView(this)
            tube.setColors(colors)

            // Set layout params for the grid
            val params = GridLayout.LayoutParams()
            params.width = 0 // Use 0 for width with weight
            params.height = GridLayout.LayoutParams.WRAP_CONTENT // Wrap content for height
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute evenly
            params.setMargins(16, 16, 16, 16)
            tube.layoutParams = params

            tube.setOnClickListener(::onTubeClicked)
            gameGrid.addView(tube)
        }
    }

    private fun onTubeClicked(view: View) {
        val clickedTube = view as TestTubeView

        if (selectedTube == null) {
            // This is the first tube selected
            if (clickedTube.isEmpty() || clickedTube.isSolved()) {
                return // Can't pour from an empty or solved tube
            }
            selectedTube = clickedTube
            clickedTube.isHighlighted = true
        } else {
            // This is the second tube (destination)
            if (selectedTube == clickedTube) {
                // User clicked the same tube twice, deselect it
                selectedTube?.isHighlighted = false
                selectedTube = null
            } else {
                // Try to pour
                if (canPour(selectedTube!!, clickedTube)) {
                    performPour(selectedTube!!, clickedTube)
                    moves++
                    tvMoves.text = "Moves: $moves"

                    if (checkWinCondition()) {
                        showGameWonDialog()
                    }
                }
                // Deselect the first tube regardless
                selectedTube?.isHighlighted = false
                selectedTube = null
            }
        }
    }

    private fun canPour(source: TestTubeView, dest: TestTubeView): Boolean {
        if (dest.isFull()) return false

        val colorToPour = source.getTopColor() ?: return false
        val topColorOnDest = dest.getTopColor()

        // Can pour if dest is empty or top colors match
        return topColorOnDest == null || topColorOnDest == colorToPour
    }

    private fun performPour(source: TestTubeView, dest: TestTubeView) {
        val colorsToMove = source.removeTopColors()
        dest.addColors(colorsToMove)
    }

    private fun checkWinCondition(): Boolean {
        for (i in 0 until gameGrid.childCount) {
            val tube = gameGrid.getChildAt(i) as TestTubeView
            if (!tube.isSolved()) {
                return false
            }
        }
        return true
    }

    // --- CHANGED: Dialog now has "Play Again" (shuffled) and "Exit" ---
    private fun showGameWonDialog() {
        val message = "You solved $currentDifficultyName in $moves moves!"
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                // This will reload the same difficulty but with shuffled colors
                loadDifficulty(currentDifficulty, shuffle = true)
            }
            .setNegativeButton("Exit") { _, _ ->
                // This will close the WaterSortActivity
                finish()
            }
            .setCancelable(false)
            .show()
    }
    // -----------------------------------------------------------------
}