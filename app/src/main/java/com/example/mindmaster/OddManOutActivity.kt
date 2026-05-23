package com.example.mindmaster

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import java.util.Random

class OddManOutActivity : AppCompatActivity() {


    private lateinit var difficultySelector: LinearLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button

    // Game UI
    private lateinit var gameContainer: LinearLayout
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvLives: TextView
    private lateinit var emojiGrid: GridLayout

    // Game Logic
    private var currentDifficulty = Difficulty.EASY
    private var score = 0
    private var lives = 3
    private var levelIndex = 0
    private val totalLevelsPerGame = 10
    private var timer: CountDownTimer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isAnswerable = true
    private lateinit var currentPuzzleList: MutableList<EmojiPuzzle>
    private var currentOddEmoji: String = ""

    private val random = Random()


    enum class Difficulty(val gridSize: Int) {
        EASY(5),
        MEDIUM(7),
        HARD(9)
    }


    data class EmojiPuzzle(
        val normalEmoji: String,
        val oddEmoji: String,
        val difficulty: Difficulty
    )


    private val allPuzzles = listOf(
        // Easy (Obvious differences)
        EmojiPuzzle("😀", "😠", Difficulty.EASY),
        EmojiPuzzle("😙", "😚", Difficulty.EASY),
        EmojiPuzzle("🍎", "❤️", Difficulty.EASY),
        EmojiPuzzle("⚽️", "🏀", Difficulty.EASY),
        EmojiPuzzle("☀️", "🌙", Difficulty.EASY),
        EmojiPuzzle("🚗", "🚲", Difficulty.EASY),
        EmojiPuzzle("❤️", "💙", Difficulty.EASY),
        EmojiPuzzle("👍", "👎", Difficulty.EASY),
        EmojiPuzzle("🍔", "🍕", Difficulty.EASY),
        EmojiPuzzle("⬆️", "⬇️", Difficulty.EASY),

        EmojiPuzzle("🐵", "🐒", Difficulty.MEDIUM),
        EmojiPuzzle("🍇", "🍈", Difficulty.MEDIUM),
        EmojiPuzzle("👕", "👚", Difficulty.MEDIUM),
        EmojiPuzzle("🚛", "🚚", Difficulty.MEDIUM),
        EmojiPuzzle("⏰", "🕰️", Difficulty.MEDIUM),
        EmojiPuzzle("✉️", "📧", Difficulty.MEDIUM),
        EmojiPuzzle("🎉", "🎊", Difficulty.MEDIUM),
        EmojiPuzzle("📖", "📕", Difficulty.MEDIUM),
        EmojiPuzzle("👓", "🕶️", Difficulty.MEDIUM),
        EmojiPuzzle("🌎", "🌍", Difficulty.MEDIUM),

        EmojiPuzzle("🙂", "🙃", Difficulty.HARD),
        EmojiPuzzle("😑", "😐", Difficulty.HARD),
        EmojiPuzzle("🤔", "🤨", Difficulty.HARD),
        EmojiPuzzle("🏃", "🏃‍♀️", Difficulty.HARD),
        EmojiPuzzle("🐌", "🐛", Difficulty.HARD),
        EmojiPuzzle("🌵", "🎄", Difficulty.HARD),
        EmojiPuzzle("☄️", "🌠", Difficulty.HARD),
        EmojiPuzzle("📁", "📂", Difficulty.HARD),
        EmojiPuzzle("✔️", "✅", Difficulty.HARD),
        EmojiPuzzle("⚫️", "🌑", Difficulty.HARD)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_odd_man_out)


        difficultySelector = findViewById(R.id.difficultySelector)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)


        gameContainer = findViewById(R.id.gameContainer)
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        tvLives = findViewById(R.id.tvLives)
        emojiGrid = findViewById(R.id.emojiGrid)


        btnEasy.setOnClickListener { loadGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { loadGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { loadGame(Difficulty.HARD) }
    }

    private fun loadGame(difficulty: Difficulty) {
        currentDifficulty = difficulty
        score = 0
        lives = 3
        levelIndex = 0
        isAnswerable = true

        tvScore.text = "Score: $score"
        tvLives.text = "Lives: $lives"


        currentPuzzleList = allPuzzles.filter { it.difficulty == difficulty }
            .shuffled()
            .take(totalLevelsPerGame)
            .toMutableList()

        difficultySelector.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        nextLevel()
    }

    private fun nextLevel() {
        if (levelIndex >= totalLevelsPerGame) {
            if (lives > 0) {
                showGameDialog(true) // Game won
            }
            return
        }

        levelIndex++
        isAnswerable = true
        emojiGrid.removeAllViews()

        val puzzle = currentPuzzleList[levelIndex - 1]
        currentOddEmoji = puzzle.oddEmoji
        val normalEmoji = puzzle.normalEmoji
        val gridSize = currentDifficulty.gridSize

        emojiGrid.columnCount = gridSize
        emojiGrid.rowCount = gridSize

        val totalItems = gridSize * gridSize
        val oddOneIndex = random.nextInt(totalItems)


        val emojiTextSize = when(gridSize) {
            5 -> 32f
            7 -> 24f
            9 -> 18f
            else -> 40f
        }


        for (i in 0 until totalItems) {
            val emojiTextView = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            emojiTextView.layoutParams = params

            emojiTextView.gravity = Gravity.CENTER
            emojiTextView.textSize = emojiTextSize
            emojiTextView.isClickable = true
            emojiTextView.isFocusable = true
            emojiTextView.setBackgroundColor(Color.TRANSPARENT)

            val emoji = if (i == oddOneIndex) currentOddEmoji else normalEmoji
            emojiTextView.text = emoji

            emojiTextView.setOnClickListener {
                if (isAnswerable) {
                    val isCorrect = it.tag as Boolean
                    handleAnswer(isCorrect, it as TextView)
                }
            }
            emojiTextView.tag = (i == oddOneIndex)

            emojiGrid.addView(emojiTextView)
        }

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                if (isAnswerable) {
                    tvTimer.text = "0s"
                    handleAnswer(false, null)
                }
            }
        }.start()
    }

    private fun handleAnswer(isCorrect: Boolean, textView: TextView?) {
        if (!isAnswerable) return
        isAnswerable = false
        timer?.cancel()

        if (isCorrect) {
            score++
            tvScore.text = "Score: $score"
            textView?.setBackgroundColor(Color.GREEN)
        } else {
            lives--
            tvLives.text = "Lives: $lives"
            textView?.setBackgroundColor(Color.RED)


            for (i in 0 until emojiGrid.childCount) {
                val child = emojiGrid.getChildAt(i)
                if (child.tag as Boolean) {
                    child.setBackgroundColor(Color.GREEN)
                    break
                }
            }
        }


        mainHandler.postDelayed({
            if (lives <= 0) {
                showGameDialog(false)
            } else {
                nextLevel()
            }
        }, 1500)
    }

    private fun showGameDialog(didWin: Boolean) {
        timer?.cancel()
        val title: String
        val message: String

        if (didWin) {
            title = "Congrats! You Won!"
            message = "Your final score: $score out of $totalLevelsPerGame"
        } else {
            title = "Game Over!"
            message = "You ran out of lives. Your final score: $score"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                loadGame(currentDifficulty)
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        mainHandler.removeCallbacksAndMessages(null)
    }
}

