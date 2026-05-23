package com.example.mindmaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.random.Random

class GameboardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Listener to communicate with the Activity
    interface GameListener {
        fun onScoreUpdated(score: Int)
        fun onGameOver(won: Boolean)
    }
    var listener: GameListener? = null

    // Game state
    private var gridSize = 4
    private var grid: Array<Array<Int>> = Array(gridSize) { Array(gridSize) { 0 } }
    private var score = 0
    private var gameWon = false
    private var gameOver = false
    val currentDifficulty: Int
        get() = gridSize

    // Drawing properties
    private val paint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
    }
    private val tileColors = mutableMapOf<Int, Int>()
    private val textColors = mutableMapOf<Int, Int>()

    private var cellSize = 0f
    private val cellPadding = 16f
    private val cornerRadius = 12f

    // Touch handling
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (gameOver || gameWon) return false

            val dx = e2.x - (e1?.x ?: 0f)
            val dy = e2.y - (e1?.y ?: 0f)
            var moved = false

            if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                // Horizontal swipe
                if (kotlin.math.abs(dx) > swipeThreshold && kotlin.math.abs(velocityX) > swipeVelocityThreshold) {
                    if (dx > 0) moved = moveRight() else moved = moveLeft()
                }
            } else {
                // Vertical swipe
                if (kotlin.math.abs(dy) > swipeThreshold && kotlin.math.abs(velocityY) > swipeVelocityThreshold) {
                    if (dy > 0) moved = moveDown() else moved = moveUp()
                }
            }

            if (moved) {
                addRandomTile()
                checkGameState()
                invalidate() // Redraw the board
            }
            return true
        }
    })

    init {
        loadColors()
        initGame(4) // Default to Easy

        isClickable = true
        isFocusable = true
    }

    private fun loadColors() {
        // Tile Backgrounds
        tileColors[0] = ContextCompat.getColor(context, R.color.tile_empty)
        tileColors[2] = ContextCompat.getColor(context, R.color.tile_2)
        tileColors[4] = ContextCompat.getColor(context, R.color.tile_4)
        tileColors[8] = ContextCompat.getColor(context, R.color.tile_8)
        tileColors[16] = ContextCompat.getColor(context, R.color.tile_16)
        tileColors[32] = ContextCompat.getColor(context, R.color.tile_32)
        tileColors[64] = ContextCompat.getColor(context, R.color.tile_64)
        tileColors[128] = ContextCompat.getColor(context, R.color.tile_128)
        tileColors[256] = ContextCompat.getColor(context, R.color.tile_256)
        tileColors[512] = ContextCompat.getColor(context, R.color.tile_512)
        tileColors[1024] = ContextCompat.getColor(context, R.color.tile_1024)

        // --- This was the minor typo fix ---
        tileColors[2048] = ContextCompat.getColor(context, R.color.tile_2048)

        // Text Colors
        val darkText = ContextCompat.getColor(context, R.color.text_dark)
        val lightText = ContextCompat.getColor(context, R.color.text_light)
        textColors[2] = darkText
        textColors[4] = darkText
        textColors[8] = lightText
        textColors[16] = lightText
        textColors[32] = lightText
        textColors[64] = lightText
        textColors[128] = lightText
        textColors[256] = lightText
        textColors[512] = lightText
        textColors[1024] = lightText
        textColors[2048] = lightText
    }

    fun initGame(difficulty: Int) {
        gridSize = difficulty
        grid = Array(gridSize) { Array(gridSize) { 0 } }
        score = 0
        gameWon = false
        gameOver = false
        listener?.onScoreUpdated(score)
        addRandomTile()
        addRandomTile()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val boardSize = min(w, h)
        cellSize = (boardSize - cellPadding * (gridSize + 1)) / gridSize.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                drawTile(canvas, grid[r][c], r, c)
            }
        }
    }

    private fun drawTile(canvas: Canvas, value: Int, r: Int, c: Int) {
        val left = cellPadding + c * (cellSize + cellPadding)
        val top = cellPadding + r * (cellSize + cellPadding)
        val right = left + cellSize
        val bottom = top + cellSize

        val rect = RectF(left, top, right, bottom)

        val tileBgColor = tileColors[value] ?: if (value > 2048) {
            ContextCompat.getColor(context, R.color.tile_large)
        } else {
            tileColors[0]!!
        }
        paint.color = tileBgColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        if (value > 0) {
            val tileTextColor = textColors[value] ?:
            ContextCompat.getColor(context, R.color.text_light)

            textPaint.color = tileTextColor

            textPaint.textSize = when {
                value < 100 -> cellSize * 0.5f
                value < 1000 -> cellSize * 0.4f
                else -> cellSize * 0.3f
            }

            val text = value.toString()
            val textBounds = android.graphics.Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val textY = top + cellSize / 2 - textBounds.exactCenterY()
            canvas.drawText(text, left + cellSize / 2, textY, textPaint)
        }
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] == 0) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            grid[r][c] = if (Random.nextInt(10) == 0) 4 else 2
        }
    }

    // --- YOUR ORIGINAL MOVE LOGIC (WHICH IS CORRECT) ---

    private fun moveLeft(): Boolean {
        var moved = false
        var scoreGained = 0 // Use local var
        for (r in 0 until gridSize) {
            val row = grid[r].filter { it > 0 }.toMutableList()
            val mergedRow = mutableListOf<Int>()
            var i = 0

            while (i < row.size) {
                if (i + 1 < row.size && row[i] == row[i + 1]) {
                    val newValue = row[i] * 2
                    mergedRow.add(newValue)
                    scoreGained += newValue
                    if (newValue == 2048) gameWon = true
                    i += 2
                    moved = true
                } else {
                    mergedRow.add(row[i])
                    i++
                }
            }

            val newRow = Array(gridSize) { 0 }
            for (j in mergedRow.indices) {
                newRow[j] = mergedRow[j]
            }

            if (!grid[r].contentEquals(newRow)) {
                moved = true
            }
            grid[r] = newRow
        }
        if (moved) {
            score += scoreGained
            listener?.onScoreUpdated(score)
        }
        return moved
    }

    private fun moveRight(): Boolean {
        var moved = false
        var scoreGained = 0
        for (r in 0 until gridSize) {
            val originalRow = grid[r].clone()

            val row = grid[r].filter { it > 0 }.toMutableList()
            row.reverse()

            val mergedRow = mutableListOf<Int>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i] == row[i + 1]) {
                    val newValue = row[i] * 2
                    mergedRow.add(newValue)
                    scoreGained += newValue
                    if (newValue == 2048) gameWon = true
                    i += 2
                    moved = true
                } else {
                    mergedRow.add(row[i])
                    i++
                }
            }

            mergedRow.reverse()

            val newRow = Array(gridSize) { 0 }
            for (j in 0 until mergedRow.size) {
                newRow[gridSize - mergedRow.size + j] = mergedRow[j]
            }

            if (!originalRow.contentEquals(newRow)) {
                moved = true
            }
            grid[r] = newRow
        }
        if (moved) {
            score += scoreGained
            listener?.onScoreUpdated(score)
        }
        return moved
    }


    private fun moveUp(): Boolean {
        var moved = false
        var scoreGained = 0
        val transposed = transpose()

        for (r in 0 until gridSize) {
            val row = transposed[r].filter { it > 0 }.toMutableList()
            val mergedRow = mutableListOf<Int>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i] == row[i + 1]) {
                    val newValue = row[i] * 2
                    mergedRow.add(newValue)
                    scoreGained += newValue
                    if (newValue == 2048) gameWon = true
                    i += 2
                    moved = true
                } else {
                    mergedRow.add(row[i])
                    i++
                }
            }

            val newRow = Array(gridSize) { 0 }
            for (j in mergedRow.indices) {
                newRow[j] = mergedRow[j]
            }

            if (!transposed[r].contentEquals(newRow)) {
                moved = true
            }
            transposed[r] = newRow
        }

        // Call the FIXED transposeBack function
        grid = transposed.transposeBack()

        if (moved) {
            score += scoreGained
            listener?.onScoreUpdated(score)
        }
        return moved
    }

    private fun moveDown(): Boolean {
        var moved = false
        var scoreGained = 0
        val transposed = transpose()

        for (r in 0 until gridSize) {
            val originalRow = transposed[r].clone()

            val row = transposed[r].filter { it > 0 }.toMutableList()
            row.reverse()

            val mergedRow = mutableListOf<Int>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i] == row[i + 1]) {
                    val newValue = row[i] * 2
                    mergedRow.add(newValue)
                    scoreGained += newValue
                    if (newValue == 2048) gameWon = true
                    i += 2
                    moved = true
                } else {
                    mergedRow.add(row[i])
                    i++
                }
            }

            mergedRow.reverse()

            val newRow = Array(gridSize) { 0 }
            for (j in 0 until mergedRow.size) {
                newRow[gridSize - mergedRow.size + j] = mergedRow[j]
            }

            if (!originalRow.contentEquals(newRow)) {
                moved = true
            }
            transposed[r] = newRow
        }

        // Call the FIXED transposeBack function
        grid = transposed.transposeBack()

        if (moved) {
            score += scoreGained
            listener?.onScoreUpdated(score)
        }
        return moved
    }

    // Helper to transpose the grid for up/down moves
    private fun transpose(): Array<Array<Int>> {
        val transposed = Array(gridSize) { Array(gridSize) { 0 } }
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                transposed[c][r] = grid[r][c]
            }
        }
        return transposed
    }

    // --- THIS IS THE ONLY MAJOR FIX ---
    // Helper to transpose back
    private fun Array<Array<Int>>.transposeBack(): Array<Array<Int>> {
        val transposed = Array(gridSize) { Array(gridSize) { 0 } }
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // The indices are swapped: [r][c] = this[c][r]
                transposed[r][c] = this[c][r]
            }
        }
        return transposed
    }
    // --- END FIX ---

    private fun checkGameState() {
        if (gameWon) {
            listener?.onGameOver(true) // Pass true for win
            return
        }

        if (isBoardFull() && !canMove()) {
            gameOver = true
            listener?.onGameOver(false) // Pass false for loss
        }
    }

    private fun isBoardFull(): Boolean {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] == 0) return false
            }
        }
        return true
    }

    private fun canMove(): Boolean {
        // Check for adjacent identical tiles
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // Check right
                if (c + 1 < gridSize && grid[r][c] == grid[r][c + 1]) return true
                // Check down
                if (r + 1 < gridSize && grid[r][c] == grid[r + 1][c]) return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pass all touch events to the gesture detector
        return gestureDetector.onTouchEvent(event)
    }
}