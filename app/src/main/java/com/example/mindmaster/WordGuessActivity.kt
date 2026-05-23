package com.example.mindmaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class WordCrossActivity : AppCompatActivity() {

    // Difficulty selection
    private lateinit var difficultySelector: LinearLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button

    // Game UI
    private lateinit var gameContainer: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvSkips: TextView
    private lateinit var tvScrambledWord: TextView
    private lateinit var editTextGuess: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnSkip: Button
    private lateinit var cardView: MaterialCardView // To flash colors

    // Game Logic
    private var currentDifficulty = Difficulty.EASY
    private var score = 0
    private var skipsLeft = 3
    private var currentWord = ""
    private var questionCount = 0
    private val totalQuestionsPerGame = 10
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentWordList = mutableListOf<String>()

    // Word lists
    private val easyWords = listOf("FOUR", "TREE", "BOOK", "MILK", "FISH", "BIRD", "BLUE", "CAKE", "SHIP", "HAND")
    private val mediumWords = listOf("APPLE", "TABLE", "CHAIR", "GREEN", "WATER", "HOUSE", "MOUSE", "SMILE", "TRAIN", "EARTH")
    private val hardWords = listOf("ANDROID", "KOTLIN", "PROJECT", "ACTIVITY", "FRAGMENT", "COMPUTER", "PROGRAM", "LANGUAGE", "KEYBOARD", "PUZZLE")


    enum class Difficulty { EASY, MEDIUM, HARD }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_cross)

        // Find difficulty views
        difficultySelector = findViewById(R.id.difficultySelector)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)

        // Find game views
        gameContainer = findViewById(R.id.gameContainer)
        tvScore = findViewById(R.id.tvScore)
        tvSkips = findViewById(R.id.tvSkips)
        tvScrambledWord = findViewById(R.id.tvScrambledWord)
        cardView = tvScrambledWord.parent as MaterialCardView
        editTextGuess = findViewById(R.id.editTextGuess)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnSkip = findViewById(R.id.btnSkip)

        // Set listeners
        btnEasy.setOnClickListener { loadGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { loadGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { loadGame(Difficulty.HARD) }
        btnSubmit.setOnClickListener { checkAnswer() }
        btnSkip.setOnClickListener { skipWord() }
    }

    private fun loadGame(difficulty: Difficulty) {
        currentDifficulty = difficulty
        score = 0
        skipsLeft = 3
        questionCount = 0

        tvScore.text = "Score: 0"
        updateSkipsUI()

        // Load and shuffle the correct word list
        currentWordList = when (difficulty) {
            Difficulty.EASY -> easyWords.toMutableList()
            Difficulty.MEDIUM -> mediumWords.toMutableList()
            Difficulty.HARD -> hardWords.toMutableList()
        }
        currentWordList.shuffle()

        difficultySelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        nextWord()
    }

    private fun nextWord() {
        if (questionCount >= totalQuestionsPerGame || currentWordList.isEmpty()) {
            showGameDialog(true) // Game won
            return
        }

        questionCount++
        currentWord = currentWordList.removeAt(0) // Get and remove the next word
        tvScrambledWord.text = scrambleWord(currentWord)
        editTextGuess.text.clear()
    }

    private fun scrambleWord(word: String): String {
        var scrambled: String
        do {
            // Convert to list, shuffle, and join back to a string
            scrambled = word.toList().shuffled().joinToString("")
        } while (scrambled == word && word.length > 1) // Ensure it's not the original word
        return scrambled
    }

    private fun checkAnswer() {
        val guess = editTextGuess.text.toString().trim().uppercase()

        if (guess == currentWord) {
            score++
            tvScore.text = "Score: $score"
            flashFeedback(true) // Flash green for correct
            mainHandler.postDelayed({
                nextWord()
            }, 500) // Wait half a second, then show next word
        } else {
            flashFeedback(false) // Flash red for incorrect
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun skipWord() {
        if (skipsLeft > 0) {
            skipsLeft--
            updateSkipsUI()
            Toast.makeText(this, "Skipped! The word was $currentWord", Toast.LENGTH_SHORT).show()
            mainHandler.postDelayed({
                nextWord()
            }, 500)
        } else {
            Toast.makeText(this, "No skips left!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun flashFeedback(isCorrect: Boolean) {
        val color = if (isCorrect) Color.GREEN else Color.RED
        val originalColor = cardView.cardBackgroundColor.defaultColor

        cardView.setCardBackgroundColor(color)
        mainHandler.postDelayed({
            cardView.setCardBackgroundColor(originalColor)
        }, 500) // Flash duration
    }

    private fun updateSkipsUI() {
        tvSkips.text = "Skips: $skipsLeft"
        btnSkip.isEnabled = skipsLeft > 0
    }

    private fun showGameDialog(didWin: Boolean) {
        val title = if (didWin) "You Win!" else "Game Over"
        val message = "Your final score: $score"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                loadGame(currentDifficulty) // Replay same difficulty
            }
            .setNegativeButton("Select Difficulty") { _, _ ->
                showDifficultyScreen()
            }
            .setCancelable(false)
            .show()
    }

    private fun showDifficultyScreen() {
        gameContainer.visibility = View.GONE
        difficultySelector.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
