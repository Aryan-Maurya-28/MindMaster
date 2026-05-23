package com.example.mindmaster

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmaster.SudokuBoardView
import com.example.mindmaster.SudokuPuzzles

class SudokuActivity : AppCompatActivity() {

    private lateinit var levelSelector: View
    private lateinit var gameContainer: View
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var tvLevel: TextView
    private lateinit var tvMistakes: TextView
    private lateinit var btnReset: Button
    private lateinit var btnEasy: Button
    private lateinit var btnNormal: Button
    private lateinit var btnHard: Button
    private val numberButtons = mutableListOf<Button>()

    private var mistakes = 0
    private var currentLevel = 1
    private var currentDifficultyName = "Easy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        levelSelector = findViewById(R.id.levelSelector)
        gameContainer = findViewById(R.id.gameContainer)
        sudokuBoardView = findViewById(R.id.sudokuBoardView)
        tvLevel = findViewById(R.id.tvLevel)
        tvMistakes = findViewById(R.id.tvMistakes)
        btnReset = findViewById(R.id.btnReset)
        btnEasy = findViewById(R.id.btnEasy)
        btnNormal = findViewById(R.id.btnNormal)
        btnHard = findViewById(R.id.btnHard)

        // Set listeners for difficulty buttons
        btnEasy.setOnClickListener { loadLevel(1) }
        btnNormal.setOnClickListener { loadLevel(2) }
        btnHard.setOnClickListener { loadLevel(3) }

        btnReset.setOnClickListener { resetGame() }

        // Find and set listeners for number buttons
        setupNumberButtons()

        // Set the callback from the board view
        sudokuBoardView.setGameCallback(object : SudokuBoardView.GameCallback {
            override fun onMoveMade(isCorrect: Boolean) {
                if (!isCorrect) {
                    mistakes++
                    tvMistakes.text = "Mistakes: $mistakes"
                }
            }

            override fun onGameWon() {
                showGameWonDialog()
            }
        })
    }

    private fun setupNumberButtons() {
        val buttonIds = listOf(
            R.id.btnNum1, R.id.btnNum2, R.id.btnNum3,
            R.id.btnNum4, R.id.btnNum5, R.id.btnNum6,
            R.id.btnNum7, R.id.btnNum8, R.id.btnNum9,
            R.id.btnErase
        )

        for (id in buttonIds) {
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                if (it.id == R.id.btnErase) {
                    sudokuBoardView.eraseNumber()
                } else {
                    val number = button.text.toString().toInt()
                    sudokuBoardView.setNumber(number)
                }
            }
            numberButtons.add(button)
        }
    }

    private fun loadLevel(level: Int) {
        currentLevel = level
        currentDifficultyName = when(level) {
            1 -> "Easy"
            2 -> "Normal"
            3 -> "Hard"
            else -> "Easy"
        }

        mistakes = 0
        tvLevel.text = "Level: $currentDifficultyName"
        tvMistakes.text = "Mistakes: 0"

        val (puzzle, solution) = SudokuPuzzles.getPuzzle(level)
        sudokuBoardView.setPuzzle(puzzle, solution)

        levelSelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE
    }

    private fun resetGame() {
        loadLevel(currentLevel)
    }

    private fun showGameWonDialog() {
        val message = "You solved $currentDifficultyName with $mistakes mistakes!"
        AlertDialog.Builder(this)
            .setTitle("You Won!")
            .setMessage(message)
            .setPositiveButton("New Game") { _, _ ->
                gameContainer.visibility = View.GONE
                levelSelector.visibility = View.VISIBLE
            }
            .setNegativeButton("Replay") { _, _ ->
                resetGame()
            }
            .setCancelable(false)
            .show()
    }
}
