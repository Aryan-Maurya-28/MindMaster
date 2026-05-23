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

class GeneralKnowledgeActivity : AppCompatActivity(), View.OnClickListener {

    // Difficulty selection
    private lateinit var difficultySelector: LinearLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button

    // Game UI
    private lateinit var gameContainer: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionCount: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvLives: TextView // <-- NEW: For life count
    private lateinit var btnAnswer1: Button
    private lateinit var btnAnswer2: Button
    private lateinit var btnAnswer3: Button
    private lateinit var btnAnswer4: Button
    private lateinit var answerButtons: List<Button>

    // Game Logic
    private var currentDifficulty = Difficulty.EASY
    private var score = 0
    private var lives = 3 // <-- NEW: Life system
    private var questionIndex = 0
    private val totalQuestionsPerGame = 10
    private var timer: CountDownTimer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isAnswerable = true
    private lateinit var currentQuestionList: MutableList<Question>
    private var currentCorrectAnswer: String = ""

    enum class Difficulty { EASY, MEDIUM, HARD }

    data class Question(
        val question: String,
        val options: List<String>, // Must contain 4 items, one of which is the correct answer
        val correctAnswer: String,
        val difficulty: Difficulty
    )

    // Your question database
    private val allQuestions = listOf(
        // --- Easy (10 questions) ---
        Question("What is the capital of France?", listOf("Paris", "London", "Berlin", "Rome"), "Paris", Difficulty.EASY),
        Question("Which planet is known as the Red Planet?", listOf("Earth", "Mars", "Jupiter", "Venus"), "Mars", Difficulty.EASY),
        Question("How many continents are there?", listOf("5", "6", "7", "8"), "7", Difficulty.EASY),
        Question("What is the largest mammal?", listOf("Elephant", "Blue Whale", "Giraffe", "Shark"), "Blue Whale", Difficulty.EASY),
        Question("What color is a banana?", listOf("Red", "Blue", "Yellow", "Green"), "Yellow", Difficulty.EASY),
        Question("What sound does a dog make?", listOf("Moo", "Meow", "Bark", "Oink"), "Bark", Difficulty.EASY),
        Question("How many legs does a spider have?", listOf("6", "8", "10", "4"), "8", Difficulty.EASY),
        Question("What do bees make?", listOf("Milk", "Honey", "Silk", "Bread"), "Honey", Difficulty.EASY),
        Question("What is the opposite of 'Hot'?", listOf("Warm", "Cold", "Spicy", "Ice"), "Cold", Difficulty.EASY),
        Question("Which is the fastest land animal?", listOf("Lion", "Tiger", "Cheetah", "Horse"), "Cheetah", Difficulty.EASY),

        // --- Medium (10 questions) ---
        Question("Who painted the Mona Lisa?", listOf("Van Gogh", "Picasso", "Da Vinci", "Monet"), "Da Vinci", Difficulty.MEDIUM),
        Question("What is the chemical symbol for water?", listOf("H2O", "CO2", "O2", "NaCl"), "H2O", Difficulty.MEDIUM),
        Question("What is the tallest mountain in the world?", listOf("K2", "Kangchenjunga", "Makalu", "Mount Everest"), "Mount Everest", Difficulty.MEDIUM),
        Question("What country is the Great Pyramid of Giza in?", listOf("Mexico", "Egypt", "Peru", "China"), "Egypt", Difficulty.MEDIUM),
        Question("Who wrote 'Romeo and Juliet'?", listOf("Shakespeare", "Dickens", "Twain", "Wilde"), "Shakespeare", Difficulty.MEDIUM),
        Question("What is the currency of Japan?", listOf("Dollar", "Euro", "Yuan", "Yen"), "Yen", Difficulty.MEDIUM),
        Question("Which ocean is the largest?", listOf("Atlantic", "Indian", "Arctic", "Pacific"), "Pacific", Difficulty.MEDIUM),
        Question("What gas do plants absorb from the atmosphere?", listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"), "Carbon Dioxide", Difficulty.MEDIUM),
        Question("How many sides does a hexagon have?", listOf("5", "6", "7", "8"), "6", Difficulty.MEDIUM),
        Question("What is the main ingredient in bread?", listOf("Sugar", "Flour", "Eggs", "Milk"), "Flour", Difficulty.MEDIUM),

        // --- Hard (10 questions) ---
        Question("What is the powerhouse of the cell?", listOf("Nucleus", "Ribosome", "Mitochondria", "Chloroplast"), "Mitochondria", Difficulty.HARD),
        Question("In what year did World War II end?", listOf("1945", "1918", "1939", "1950"), "1945", Difficulty.HARD),
        Question("What is the speed of light?", listOf("300,000 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s"), "300,000 km/s", Difficulty.HARD),
        Question("Who discovered penicillin?", listOf("Marie Curie", "Albert Einstein", "Isaac Newton", "Alexander Fleming"), "Alexander Fleming", Difficulty.HARD),
        Question("What is the hardest natural substance on Earth?", listOf("Gold", "Iron", "Diamond", "Quartz"), "Diamond", Difficulty.HARD),
        Question("Who developed the theory of relativity?", listOf("Newton", "Galileo", "Einstein", "Tesla"), "Einstein", Difficulty.HARD),
        Question("What is the capital of Australia?", listOf("Sydney", "Melbourne", "Brisbane", "Canberra"), "Canberra", Difficulty.HARD),
        Question("What is the square root of 144?", listOf("10", "11", "12", "13"), "12", Difficulty.HARD),
        Question("Which element has the atomic number 1?", listOf("Helium", "Oxygen", "Hydrogen", "Carbon"), "Hydrogen", Difficulty.HARD),
        Question("What is the longest river in the world?", listOf("Amazon", "Nile", "Mississippi", "Yangtze"), "Nile", Difficulty.HARD)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_knowledge)

        // Find difficulty views
        difficultySelector = findViewById(R.id.difficultySelector)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)

        // Find game views
        gameContainer = findViewById(R.id.gameContainer)
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvLives = findViewById(R.id.tvLives) // <-- NEW: Find the TextView
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
        lives = 3 // <-- NEW: Reset lives
        questionIndex = 0
        isAnswerable = true

        tvScore.text = "Score: $score"
        tvLives.text = "Lives: $lives" // <-- NEW: Update UI

        // Get 10 random questions for the chosen difficulty
        currentQuestionList = allQuestions.filter { it.difficulty == difficulty }
            .shuffled()
            .take(totalQuestionsPerGame)
            .toMutableList()

        difficultySelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        nextQuestion()
    }

    private fun nextQuestion() {
        // --- CHANGED: Check for win condition here ---
        if (questionIndex >= totalQuestionsPerGame) {
            if (lives > 0) {
                showGameDialog(true) // Game won
            }
            return
        }
        // ------------------------------------------

        questionIndex++
        isAnswerable = true
        resetButtonColors()

        val question = currentQuestionList[questionIndex - 1] // Get current question
        currentCorrectAnswer = question.correctAnswer

        tvQuestion.text = question.question
        tvQuestionCount.text = "Q: $questionIndex/$totalQuestionsPerGame"

        val shuffledOptions = question.options.shuffled()
        answerButtons.forEachIndexed { index, button ->
            button.text = shuffledOptions[index]
        }

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(15000, 1000) { // 15 second timer
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                if (isAnswerable) {
                    tvTimer.text = "0s"
                    handleAnswer(false, null) // Time's up
                }
            }
        }.start()
    }

    override fun onClick(v: View?) {
        if (!isAnswerable) return // Don't allow multiple clicks

        timer?.cancel()
        isAnswerable = false
        val clickedButton = v as Button
        val isCorrect = clickedButton.text.toString() == currentCorrectAnswer

        handleAnswer(isCorrect, clickedButton)
    }

    private fun handleAnswer(isCorrect: Boolean, button: Button?) {
        if (isCorrect) {
            score++
            tvScore.text = "Score: $score"
            button?.setBackgroundColor(Color.GREEN)
        } else {
            lives-- // <-- NEW: Lose a life
            tvLives.text = "Lives: $lives" // <-- NEW: Update UI
            button?.setBackgroundColor(Color.RED)
            // Highlight the correct answer
            answerButtons.find { it.text.toString() == currentCorrectAnswer }
                ?.setBackgroundColor(Color.GREEN)
        }

        // Wait 1.5 seconds, then check if game over or next question
        mainHandler.postDelayed({
            if (lives <= 0) {
                showGameDialog(false) // <-- NEW: Game over
            } else {
                nextQuestion()
            }
        }, 1500)
    }

    private fun resetButtonColors() {
        answerButtons.forEach {
            it.setBackgroundColor(Color.parseColor("#6200EE")) // Default button color
        }
    }
    // NOTE: Please note that #6200EE is the default purple color for Material buttons. You might need to adjust this hex code if you have a custom app theme.

    private fun showGameDialog(didWin: Boolean) {
        timer?.cancel()

        // --- CHANGED: Updated titles and messages ---
        val title: String
        val message: String

        if (didWin) {
            title = "Congrats! You Won!"
            message = "Your final score: $score out of $totalQuestionsPerGame"
        } else {
            title = "Game Over!"
            message = "You ran out of lives. Your final score: $score"
        }
        // ------------------------------------------

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                loadGame(currentDifficulty) // Replay same difficulty
            }
            // --- CHANGED: "Exit" button instead of "Select Difficulty" ---
            .setNegativeButton("Exit") { _, _ ->
                finish() // Close the activity
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

