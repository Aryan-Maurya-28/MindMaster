package com.example.mindmaster

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MathQuizActivity : AppCompatActivity(), View.OnClickListener {

    // Difficulty selection
    private lateinit var difficultySelector: LinearLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button

    // Game UI
    private lateinit var gameContainer: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvLives: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var btnAnswer1: Button
    private lateinit var btnAnswer2: Button
    private lateinit var btnAnswer3: Button
    private lateinit var btnAnswer4: Button
    private lateinit var answerButtons: List<Button>

    // Game Logic
    private var currentDifficulty = Difficulty.EASY
    private var score = 0
    private var lives = 3
    private var currentQuestionText = ""
    private var currentAnswer = 0
    private var questionCount = 0
    private val totalQuestionsPerGame = 10
    private var timer: CountDownTimer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isAnswerable = true

    enum class Difficulty { EASY, MEDIUM, HARD }
    private enum class Operation { ADD, SUBTRACT, MULTIPLY, DIVIDE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_quiz)

        // Find difficulty views
        difficultySelector = findViewById(R.id.difficultySelector)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)

        // Find game views
        gameContainer = findViewById(R.id.gameContainer)
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer) // <-- Finds the timer TextView
        tvLives = findViewById(R.id.tvLives)
        tvQuestion = findViewById(R.id.tvQuestion)
        btnAnswer1 = findViewById(R.id.btnAnswer1)
        btnAnswer2 = findViewById(R.id.btnAnswer2)
        btnAnswer3 = findViewById(R.id.btnAnswer3)
        btnAnswer4 = findViewById(R.id.btnAnswer4)
        answerButtons = listOf(btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4)

        // Set listeners
        btnEasy.setOnClickListener { loadGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { loadGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { loadGame(Difficulty.HARD) }
        answerButtons.forEach { it.setOnClickListener(this) }
    }

    private fun loadGame(difficulty: Difficulty) {
        currentDifficulty = difficulty
        score = 0
        lives = 3
        questionCount = 0
        isAnswerable = true

        tvScore.text = "Score: 0"
        updateLivesUI()

        difficultySelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        nextQuestion()
    }

    private fun nextQuestion() {
        if (questionCount >= totalQuestionsPerGame) {
            showGameDialog(true) // Game won
            return
        }
        if (lives <= 0) {
            showGameDialog(false) // Game lost
            return
        }

        questionCount++
        isAnswerable = true
        resetButtonColors()
        generateQuestion()

        tvQuestion.text = currentQuestionText
        val answers = generateAnswers()

        answerButtons.shuffled().forEachIndexed { index, button ->
            button.text = answers[index].toString()
        }

        startTimer() // <-- Starts the timer every new question
    }

    private fun generateQuestion() {
        val (q, a) = when (currentDifficulty) {
            Difficulty.EASY -> generateEasyQuestion()
            Difficulty.MEDIUM -> generateMediumQuestion()
            Difficulty.HARD -> generateHardQuestion()
        }
        currentQuestionText = "$q ?"
        currentAnswer = a
    }

    private fun generateEasyQuestion(): Pair<String, Int> {
        return when (Random.nextInt(3)) {
            0 -> { // Addition
                val a = Random.nextInt(1, 21)
                val b = Random.nextInt(1, 21)
                "$a + $b" to a + b
            }
            1 -> { // Subtraction
                val a = Random.nextInt(10, 31)
                val b = Random.nextInt(1, a) // Ensure positive result
                "$a - $b" to a - b
            }
            else -> { // Combination
                val a = Random.nextInt(1, 11)
                val b = Random.nextInt(1, 11)
                val c = Random.nextInt(1, 11)
                "$a + $b - $c" to a + b - c
            }
        }
    }

    private fun generateMediumQuestion(): Pair<String, Int> {
        return when (Random.nextInt(4)) {
            0 -> { // Addition (larger numbers)
                val a = Random.nextInt(10, 51)
                val b = Random.nextInt(10, 51)
                "$a + $b" to a + b
            }
            1 -> { // Subtraction (larger numbers)
                val a = Random.nextInt(20, 61)
                val b = Random.nextInt(1, a)
                "$a - $b" to a - b
            }
            2 -> { // Multiplication
                val a = Random.nextInt(2, 13)
                val b = Random.nextInt(2, 13)
                "$a × $b" to a * b
            }
            else -> { // Combination
                val a = Random.nextInt(2, 11)
                val b = Random.nextInt(2, 11)
                val c = Random.nextInt(1, 21)
                if (Random.nextBoolean()) {
                    "$a × $b + $c" to a * b + c
                } else {
                    "$a × $b - $c" to a * b - c
                }
            }
        }
    }

    private fun generateHardQuestion(): Pair<String, Int> {
        return when (Random.nextInt(5)) {
            0 -> { // Multiplication (larger)
                val a = Random.nextInt(5, 21)
                val b = Random.nextInt(5, 21)
                "$a × $b" to a * b
            }
            1 -> { // Division (no remainder)
                val b = Random.nextInt(2, 11)
                val result = Random.nextInt(2, 11)
                val a = b * result
                "$a ÷ $b" to result
            }
            2 -> { // Combo with Multiply
                val a = Random.nextInt(2, 11)
                val b = Random.nextInt(2, 11)
                val c = Random.nextInt(1, 21)
                "$a × $b + $c" to a * b + c
            }
            3 -> { // Combo with Divide
                val b = Random.nextInt(2, 11)
                val result = Random.nextInt(2, 11)
                val a = b * result
                val c = Random.nextInt(1, 21)
                "$a ÷ $b - $c" to a / b - c
            }
            else -> { // All three
                val a = Random.nextInt(2, 11)
                val b = Random.nextInt(2, 11)
                val c = Random.nextInt(1, 11)
                "$a + $b × $c" to a + b * c // Order of operations
            }
        }
    }

    private fun generateAnswers(): List<Int> {
        val answers = mutableSetOf<Int>()
        answers.add(currentAnswer)

        // Add 3 unique wrong answers
        while (answers.size < 4) {
            val wrongRange = (currentAnswer - 10).coerceAtLeast(1)..(currentAnswer + 10)
            var wrongAnswer = wrongRange.random()
            if (wrongAnswer == currentAnswer) {
                wrongAnswer += 1 // Ensure it's not the same
            }
            answers.add(wrongAnswer)
        }
        return answers.toList()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}s" // <-- Updates the text
            }

            override fun onFinish() {
                if (isAnswerable) {
                    tvTimer.text = "0s"
                    handleAnswer(false, null)
                }
            }
        }.start()
    }

    override fun onClick(v: View?) {
        if (!isAnswerable) return // Don't allow multiple clicks

        timer?.cancel()
        isAnswerable = false
        val clickedButton = v as Button
        val isCorrect = clickedButton.text.toString().toInt() == currentAnswer

        handleAnswer(isCorrect, clickedButton)
    }

    private fun handleAnswer(isCorrect: Boolean, button: Button?) {
        if (isCorrect) {
            score++
            tvScore.text = "Score: $score"
            button?.setBackgroundColor(Color.GREEN)
        } else {
            lives--
            updateLivesUI()
            button?.setBackgroundColor(Color.RED)
            // Highlight the correct answer
            answerButtons.find { it.text.toString().toInt() == currentAnswer }
                ?.setBackgroundColor(Color.GREEN)
        }

        // Wait 1 second, then go to the next question
        mainHandler.postDelayed({
            nextQuestion()
        }, 1000)
    }

    private fun updateLivesUI() {
        tvLives.text = "❤️".repeat(lives.coerceAtLeast(0))
    }

    private fun resetButtonColors() {
        answerButtons.forEach {
            it.setBackgroundColor(Color.parseColor("#6200EE")) // Default button color
        }
    }
// NOTE: Please note that #6200EE is the default purple color for Material buttons. You might need to adjust this hex code if you have a custom app theme.

    private fun showGameDialog(didWin: Boolean) {
        timer?.cancel()
        val title = if (didWin) "You Win!" else "Game Over"
        val message = "Your score: $score"

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
        timer?.cancel()
        mainHandler.removeCallbacksAndMessages(null)
    }
}

