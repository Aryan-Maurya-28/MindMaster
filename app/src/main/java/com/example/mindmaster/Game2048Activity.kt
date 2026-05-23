package com.example.mindmaster

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game2048Activity : AppCompatActivity(), GameboardView.GameListener {

    private lateinit var gameboardView: GameboardView
    private lateinit var scoreTextView: TextView
    private lateinit var highScoreTextView: TextView
    private lateinit var gameOverlayText: TextView
    private lateinit var easyButton: Button
    private lateinit var normalButton: Button
    private lateinit var hardButton: Button
    private lateinit var newGameButton: Button

    private var highScore = 0
    private val PREFS_NAME = "Game2048Prefs"
    private val HIGH_SCORE_KEY = "HighScore"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_2048)

        // Load High Score
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0)

        // Find Views
        gameboardView = findViewById(R.id.gameboardView)
        scoreTextView = findViewById(R.id.scoreTextView)
        highScoreTextView = findViewById(R.id.highScoreTextView)
        gameOverlayText = findViewById(R.id.gameOverlayText)
        easyButton = findViewById(R.id.easyButton)
        normalButton = findViewById(R.id.normalButton)
        hardButton = findViewById(R.id.hardButton)
        newGameButton = findViewById(R.id.newGameButton)

        // Set up Listeners
        gameboardView.listener = this
        highScoreTextView.text = highScore.toString()

        easyButton.setOnClickListener { startNewGame(4) }
        normalButton.setOnClickListener { startNewGame(5) }
        hardButton.setOnClickListener { startNewGame(6) }
        newGameButton.setOnClickListener { startNewGame(gameboardView.currentDifficulty) }

        // Start default game
        startNewGame(4) // Default to Easy
    }

    private fun startNewGame(difficulty: Int) {
        gameOverlayText.visibility = View.GONE
        gameboardView.initGame(difficulty)
        updateScore(0) // Reset score text
    }

    override fun onScoreUpdated(score: Int) {
        updateScore(score)
    }

    private fun updateScore(score: Int) {
        scoreTextView.text = score.toString()
        if (score > highScore) {
            highScore = score
            highScoreTextView.text = highScore.toString()
            // Save new high score
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(HIGH_SCORE_KEY, highScore).apply()
        }
    }

    override fun onGameOver(won: Boolean) {
        gameOverlayText.visibility = View.VISIBLE
        gameOverlayText.text = if (won) "You Win!" else "Game Over!"
    }
}
