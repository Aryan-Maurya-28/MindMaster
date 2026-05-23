package com.example.mindmaster

// Unused imports removed
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory // <-- IMPORT ADDED BACK
// Unused import removed
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
// Unused import removed
import android.os.Bundle
import android.util.Log
// Unused imports removed
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import java.util.Random // <-- IMPORT ADDED BACK

class JigsawPuzzleActivity : AppCompatActivity() {

    private lateinit var difficultySelector: View
    private lateinit var gameGrid: GridLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button

    // --- NEW VIEWS ---
    private lateinit var scoreBar: LinearLayout
    private lateinit var bottomMenuBar: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var btnHint: Button
    private lateinit var btnReset: Button
    // -----------------

    private var puzzlePieces = mutableListOf<PuzzlePiece>()
    private var numCols = 0
    private var numRows = 0

    // --- SCORING & HINT ---
    private var currentScore = 0
    private var highScore = Int.MAX_VALUE
    private lateinit var sharedPrefs: SharedPreferences
    private var solvedBitmap: Bitmap? = null
    private var currentDifficulty = Difficulty.EASY
    private var currentImageResId = 0 // <-- THIS IS NOW USED AGAIN
    // --------------------

    // --- IMAGE LIST ADDED BACK ---
    private val allPuzzleImages = listOf(
        R.drawable.puzzle_1,
        R.drawable.puzzle_2,
        R.drawable.puzzle_3,
        R.drawable.puzzle_4,
        R.drawable.puzzle_5
    )
    // ----------------------------

    // --- NEW: For Click-to-Swap ---
    private var firstSelectedView: ImageView? = null
    // ----------------------------

    data class PuzzlePiece(val originalIndex: Int, var currentBitmap: Bitmap, var currentIndex: Int)

    enum class Difficulty(val cols: Int, val rows: Int, val key: String) {
        EASY(3, 3, "hs_easy"),
        MEDIUM(4, 4, "hs_medium"),
        HARD(5, 5, "hs_hard")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jigsaw_puzzle)

        sharedPrefs = getSharedPreferences("JigsawPuzzlePrefs", Context.MODE_PRIVATE)

        difficultySelector = findViewById(R.id.difficultySelector)
        gameGrid = findViewById(R.id.gameGrid)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)

        // Find all the new views
        scoreBar = findViewById(R.id.scoreBar)
        bottomMenuBar = findViewById(R.id.bottomMenuBar)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        btnHint = findViewById(R.id.btnHint)
        btnReset = findViewById(R.id.btnReset)

        btnEasy.setOnClickListener { loadGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { loadGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { loadGame(Difficulty.HARD) }

        btnHint.setOnClickListener { showHint() }
        btnReset.setOnClickListener { resetGame() }

        // --- REMOVED: gridDragListener ---
    }

    private fun loadGame(difficulty: Difficulty, reset: Boolean = false) {
        currentDifficulty = difficulty
        // --- IMAGE LOGIC RESTORED ---
        if (!reset) {
            currentImageResId = allPuzzleImages.random()
        }
        // -------------------------
        numCols = difficulty.cols
        numRows = difficulty.rows

        // --- FIX FOR VISIBILITY ---
        difficultySelector.visibility = View.GONE
        scoreBar.visibility = View.VISIBLE
        gameGrid.visibility = View.VISIBLE
        bottomMenuBar.visibility = View.VISIBLE // This makes your buttons visible
        // -------------------------

        gameGrid.removeAllViews()
        puzzlePieces.clear()
        currentScore = 0
        updateScore(0)
        firstSelectedView = null // Reset selection

        loadHighScore()

        gameGrid.columnCount = numCols
        gameGrid.rowCount = numRows

        try {
            // --- LOGIC RESTORED: Load from drawable ---
            // Check if images exist before trying to load
            if (currentImageResId == 0 || resources.getResourceName(currentImageResId) == null) {
                throw Exception("Image resource not found. Did you add puzzle_1.png, etc. to res/drawable?")
            }
            val originalBitmap = BitmapFactory.decodeResource(resources, currentImageResId)
            val scaledBitmap = scaleBitmapToGrid(originalBitmap)
            solvedBitmap = scaledBitmap // Save for hint
            sliceImageAndSetupBoard(scaledBitmap)

        } catch (e: Exception) {
            Log.e("JigsawPuzzle", "Failed to load bitmap: ${e.message}", e)
            Toast.makeText(this, "Failed to load puzzle image. Check Logcat.", Toast.LENGTH_LONG).show()
            goBackToMenu()
        }
    }

    /**
     * Creates a new Bitmap with colored squares and numbers.
     * --- THIS FUNCTION IS NOW REMOVED ---
     */
    // private fun createNumberedPuzzleBitmap(...) { ... }


    private fun scaleBitmapToGrid(bitmap: Bitmap): Bitmap {
        // We estimate grid size. A better way is to measure gameGrid *after* layout,
        // but this is simpler and works well with the 1:1 ratio.
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels - (32 * displayMetrics.density).toInt() // padding
        return Bitmap.createScaledBitmap(bitmap, screenWidth, screenWidth, true)
    }

    private fun sliceImageAndSetupBoard(bitmap: Bitmap) {
        val pieceWidth = bitmap.width / numCols
        val pieceHeight = bitmap.height / numRows

        var index = 0
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val pieceBitmap = Bitmap.createBitmap(bitmap, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight)
                puzzlePieces.add(PuzzlePiece(index, pieceBitmap, index))
                index++
            }
        }

        // We still shuffle the pieces
        puzzlePieces.shuffle()

        for ((i, piece) in puzzlePieces.withIndex()) {
            piece.currentIndex = i
            // The layout file list_item_puzzle_piece.xml is still needed

            // --- NEW STABILITY FIX: Use safe cast 'as?' ---
            val pieceView = LayoutInflater.from(this)
                .inflate(R.layout.list_item_puzzle_piece, gameGrid, false) as? ImageView

            // If the cast fails (e.g., list_item_puzzle_piece.xml is not an ImageView),
            // log an error and skip this piece instead of crashing.
            if (pieceView == null) {
                Log.e("JigsawPuzzle", "Failed to inflate R.layout.list_item_puzzle_piece as an ImageView. Check the layout file!")
                continue // Skip this piece
            }

            pieceView.setImageBitmap(piece.currentBitmap)
            pieceView.tag = piece

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(1, 1, 1, 1)
            pieceView.layoutParams = params

            // --- REPLACED Listeners ---
            pieceView.setOnClickListener(pieceClickListener)
            // --- REMOVED: setOnLongClickListener ---
            // --- REMOVED: setOnDragListener ---

            gameGrid.addView(pieceView)
        }
    }

    // --- !! NEW CLICK-TO-SWAP LOGIC !! ---
    private val pieceClickListener = View.OnClickListener { view ->
        val clickedView = view as? ImageView ?: return@OnClickListener

        if (firstSelectedView == null) {
            // This is the first piece clicked
            firstSelectedView = clickedView
            clickedView.alpha = 0.7f // Highlight
        } else {
            // This is the second piece clicked
            val secondSelectedView = clickedView

            if (firstSelectedView == secondSelectedView) {
                // User clicked the same piece twice, deselect it
                firstSelectedView?.alpha = 1.0f
                firstSelectedView = null
                return@OnClickListener
            }

            // --- This is the SWAP logic ---
            val firstPiece = firstSelectedView?.tag as? PuzzlePiece
            val secondPiece = secondSelectedView.tag as? PuzzlePiece

            if (firstPiece == null || secondPiece == null) {
                Log.e("JigsawPuzzle", "Click-to-swap failed, a piece has a null tag.")
                // Reset selection
                firstSelectedView?.alpha = 1.0f
                firstSelectedView = null
                return@OnClickListener
            }

            // 1. Swap the bitmaps in the UI
            firstSelectedView?.setImageBitmap(secondPiece.currentBitmap)
            secondSelectedView.setImageBitmap(firstPiece.currentBitmap)

            // 2. Swap the data in the tags
            firstSelectedView?.tag = secondPiece
            secondSelectedView.tag = firstPiece

            // 3. Update the 'currentIndex' in the data objects (optional, but good practice)
            val tempIndex = firstPiece.currentIndex
            firstPiece.currentIndex = secondPiece.currentIndex
            secondPiece.currentIndex = tempIndex

            updateScore(1) // Increment score
            checkWinCondition()

            // 4. Reset selection
            firstSelectedView?.alpha = 1.0f
            firstSelectedView = null
        }
    }
    // ----------------------------------------

    // --- REMOVED: pieceLongClickListener ---
    // --- REMOVED: pieceDragListener ---
    // --- REMOVED: gridDragListener ---


    private fun checkWinCondition() {
        for (i in 0 until puzzlePieces.size) {
            val view = gameGrid.getChildAt(i)
            if (view == null) {
                Log.e("JigsawPuzzle", "Error: Child view is null during win check.")
                return
            }
            // Use safe cast here
            val piece = view.tag as? PuzzlePiece
            if (piece == null || piece.originalIndex != i) {
                return // Not solved yet
            }
        }
        showGameWonDialog()
    }

    private fun showGameWonDialog() {
        checkHighScore()
        val message = "Congratulations! You solved the puzzle in $currentScore moves.\n" +
                if (currentScore == highScore) "That's a new best time!" else "Best: $highScore"

        AlertDialog.Builder(this)
            .setTitle("You Won!")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ -> // This goes back to the difficulty menu
                goBackToMenu()
            }
            // --- NEW: Added Exit Button ---
            .setNegativeButton("Exit") { _, _ ->
                finish() // This will close the puzzle activity
            }
            // ------------------------------
            .setCancelable(false)
            .show()
    }

    private fun goBackToMenu() {
        // --- FIX FOR VISIBILITY ---
        gameGrid.visibility = View.GONE
        scoreBar.visibility = View.GONE
        bottomMenuBar.visibility = View.GONE // Hide the buttons again
        difficultySelector.visibility = View.VISIBLE
        // -------------------------
    }

    // --- NEW FUNCTIONS for Score, Hint, Reset ---

    private fun updateScore(addMoves: Int) {
        currentScore += addMoves
        tvScore.text = "Moves: $currentScore"
    }

    private fun loadHighScore() {
        highScore = sharedPrefs.getInt(currentDifficulty.key, Int.MAX_VALUE)
        if (highScore == Int.MAX_VALUE) {
            tvHighScore.text = "Best: -"
        } else {
            tvHighScore.text = "Best: $highScore"
        }
    }

    private fun checkHighScore() {
        if (currentScore < highScore) {
            highScore = currentScore
            sharedPrefs.edit().putInt(currentDifficulty.key, highScore).apply()
            tvHighScore.text = "Best: $highScore"
        }
    }

    private fun showHint() {
        if (solvedBitmap == null) return

        val hintDialog = AlertDialog.Builder(this)
        val imageView = ImageView(this)
        imageView.setImageBitmap(solvedBitmap)
        imageView.setPadding(20, 20, 20, 20)

        hintDialog.setTitle("Hint")
        hintDialog.setView(imageView)
        hintDialog.setPositiveButton("Close", null)
        hintDialog.show()
    }

    private fun resetGame() {
        AlertDialog.Builder(this)
            .setTitle("Reset Puzzle")
            .setMessage("Are you sure you want to restart this puzzle?")
            .setPositiveButton("Yes") { _, _ ->
                // --- This will now load a new RANDOM IMAGE ---
                loadGame(currentDifficulty, reset = false)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
