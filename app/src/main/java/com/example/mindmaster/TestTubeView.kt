package com.example.mindmaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

// This is the custom view that draws the test tube and water
class TestTubeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val MAX_CAPACITY = 4
    }

    private val colors = mutableListOf<Int>()
    private var tubePath = Path()
    private var liquidPath = Path()

    // Paints
    private val tubePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.DKGRAY
    }

    private val liquidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = Color.parseColor("#FFD700") // Gold highlight
    }

    private var tubeWidth = 0f
    private var tubeHeight = 0f
    private var cornerRadius = 0f
    private var liquidHeightUnit = 0f

    // --- THIS IS THE FIX ---
    // It's renamed to avoid clashing with the built-in 'isSelected'
    var isHighlighted = false
        set(value) {
            field = value
            invalidate() // Redraw when selection changes
        }
    // -----------------------

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // --- CHANGED: Ratio changed from 1:4 to 1:3 to make tubes shorter ---
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width * 3 // Was 4, now 3
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Define dimensions based on view size
        tubeWidth = w.toFloat() - paddingLeft - paddingRight
        tubeHeight = h.toFloat() - paddingTop - paddingBottom
        cornerRadius = tubeWidth / 2f
        liquidHeightUnit = (tubeHeight - cornerRadius) / MAX_CAPACITY

        // Create the path for the tube outline
        tubePath.reset()
        // Start from top-left, just after the (non-existent) corner
        tubePath.moveTo(paddingLeft.toFloat(), paddingTop.toFloat())
        // Line to bottom-left corner
        tubePath.lineTo(paddingLeft.toFloat(), h - paddingBottom - cornerRadius)
        // Arc for the bottom
        tubePath.arcTo(
            paddingLeft.toFloat(),
            h - paddingBottom - tubeWidth,
            w - paddingRight.toFloat(),
            h - paddingBottom.toFloat(),
            180f,
            -180f,
            false
        )
        // Line to top-right
        tubePath.lineTo(w - paddingRight.toFloat(), paddingTop.toFloat())
        // We don't close it, it's an open tube
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw the liquid from bottom to top
        var currentTop = tubeHeight + paddingTop - cornerRadius
        liquidPath.reset()

        for (i in 0 until colors.size) {
            val color = colors[i]
            liquidPaint.color = color

            // Create the path for this liquid segment
            val rect = RectF(
                paddingLeft.toFloat() + tubePaint.strokeWidth / 2,
                currentTop - liquidHeightUnit,
                tubeWidth + paddingLeft - tubePaint.strokeWidth / 2,
                currentTop
            )

            if (i == 0) {
                // First (bottom) segment needs a rounded bottom
                liquidPath.addArc(
                    paddingLeft.toFloat() + tubePaint.strokeWidth / 2,
                    tubeHeight + paddingTop - tubeWidth + tubePaint.strokeWidth / 2,
                    tubeWidth + paddingLeft - tubePaint.strokeWidth / 2,
                    tubeHeight + paddingTop - tubePaint.strokeWidth / 2,
                    180f,
                    -180f
                )
            }
            liquidPath.addRect(rect, Path.Direction.CCW)

            canvas.drawPath(liquidPath, liquidPaint)
            liquidPath.reset()

            currentTop -= liquidHeightUnit
        }

        // 2. Draw the tube outline
        canvas.drawPath(tubePath, tubePaint)

        // 3. Draw highlight if selected
        // --- THIS IS THE FIX ---
        if (isHighlighted) {
            canvas.drawPath(tubePath, highlightPaint)
        }
        // -----------------------
    }

    // --- Public API for the Activity ---

    fun setColors(newColors: List<Int>) {
        colors.clear()
        colors.addAll(newColors.take(MAX_CAPACITY))
        invalidate() // Force a redraw
    }

    fun getTopColor(): Int? {
        return colors.lastOrNull()
    }

    fun isFull(): Boolean {
        return colors.size == MAX_CAPACITY
    }

    fun isEmpty(): Boolean {
        return colors.isEmpty()
    }

    fun isSolved(): Boolean {
        if (isEmpty()) return true
        if (colors.size < MAX_CAPACITY) return false
        val firstColor = colors.first()
        return colors.all { it == firstColor }
    }

    fun addColors(colorsToAdd: List<Int>) {
        for (color in colorsToAdd.reversed()) { // Add them in reverse order
            if (isFull()) break
            colors.add(color)
        }
        invalidate()
    }

    fun removeTopColors(): List<Int> {
        if (isEmpty()) return emptyList()

        val topColor = getTopColor()!!
        val colorsToMove = mutableListOf<Int>()

        // Go from top down
        while (colors.lastOrNull() == topColor) {
            colorsToMove.add(colors.removeAt(colors.size - 1))
        }

        invalidate()
        return colorsToMove
    }
}

