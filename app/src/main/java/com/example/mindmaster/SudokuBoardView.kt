package com.example.mindmaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SudokuBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val givenNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    private val userNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    private val wrongNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    private val selectedCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3E5FC") // Light blue
        style = Paint.Style.FILL
    }

    private var cellSize = 0f
    private var selectedRow = -1
    private var selectedCol = -1
    private val textBounds = Rect()

    private var puzzle = Array(9) { IntArray(9) }
    private var solution = Array(9) { IntArray(9) }
    private var userGrid = Array(9) { IntArray(9) }

    private var isGiven = Array(9) { BooleanArray(9) }
    private var isWrong = Array(9) { BooleanArray(9) }

    private var gameCallback: GameCallback? = null

    interface GameCallback {
        fun onMoveMade(isCorrect: Boolean)
        fun onGameWon()
    }

    fun setGameCallback(callback: GameCallback) {
        this.gameCallback = callback
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = (w / 9f)
        // Adjust text size based on cell size for better scaling
        givenNumberPaint.textSize = cellSize * 0.6f
        userNumberPaint.textSize = cellSize * 0.6f
        wrongNumberPaint.textSize = cellSize * 0.6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw selected cell background
        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(
                selectedCol * cellSize,
                selectedRow * cellSize,
                (selectedCol + 1) * cellSize,
                (selectedRow + 1) * cellSize,
                selectedCellPaint
            )
        }

        // 2. Draw grid lines (thin)
        for (i in 0..9) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), gridPaint)
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, gridPaint)
        }

        // 3. Draw box lines (thick)
        for (i in 0..9 step 3) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), boxPaint)
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, boxPaint)
        }

        // 4. Draw numbers
        for (r in 0..8) {
            for (c in 0..8) {
                val num = userGrid[r][c]
                if (num != 0) {
                    val numStr = num.toString()
                    val paint = if (isGiven[r][c]) {
                        givenNumberPaint
                    } else if (isWrong[r][c]) {
                        wrongNumberPaint
                    } else {
                        userNumberPaint
                    }

                    // Get text bounds to center it properly
                    paint.getTextBounds(numStr, 0, numStr.length, textBounds)
                    val x = (c * cellSize) + (cellSize / 2)
                    val y = (r * cellSize) + (cellSize / 2) - textBounds.exactCenterY()
                    canvas.drawText(numStr, x, y, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val row = (event.y / cellSize).toInt()
            val col = (event.x / cellSize).toInt()
            if (row in 0..8 && col in 0..8) {
                selectedRow = row
                selectedCol = col
                invalidate() // Redraw to show new selection
                return true
            }
        }
        return false
    }

    fun setPuzzle(newPuzzle: Array<IntArray>, newSolution: Array<IntArray>) {
        puzzle = newPuzzle
        solution = newSolution
        userGrid = Array(9) { r -> IntArray(9) { c -> newPuzzle[r][c] } }
        isGiven = Array(9) { r -> BooleanArray(9) { c -> newPuzzle[r][c] != 0 } }
        isWrong = Array(9) { BooleanArray(9) }

        selectedRow = -1
        selectedCol = -1
        invalidate()
    }

    fun setNumber(num: Int) {
        if (selectedRow == -1 || selectedCol == -1 || isGiven[selectedRow][selectedCol]) {
            return // No cell selected or cell is part of the original puzzle
        }

        val isCorrect = (solution[selectedRow][selectedCol] == num)

        userGrid[selectedRow][selectedCol] = num
        isWrong[selectedRow][selectedCol] = !isCorrect

        gameCallback?.onMoveMade(isCorrect)
        invalidate()

        if (isBoardFullAndCorrect()) {
            gameCallback?.onGameWon()
        }
    }

    fun eraseNumber() {
        if (selectedRow == -1 || selectedCol == -1 || isGiven[selectedRow][selectedCol]) {
            return
        }
        userGrid[selectedRow][selectedCol] = 0
        isWrong[selectedRow][selectedCol] = false
        invalidate()
    }

    private fun isBoardFullAndCorrect(): Boolean {
        for (r in 0..8) {
            for (c in 0..8) {
                if (userGrid[r][c] == 0 || userGrid[r][c] != solution[r][c]) {
                    return false
                }
            }
        }
        return true
    }
}
