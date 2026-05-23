package com.example.mindmaster

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout

class MemoryMatchActivity : AppCompatActivity() {

    // View variables, initialized in onCreate
    private lateinit var difficultySelector: View
    private lateinit var gameGrid: GridLayout
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    private lateinit var tvScore: TextView // For current score
    private lateinit var tvHighScore: TextView // For high score

    // A list to hold the card data models
    private val cards = mutableListOf<MemoryCard>()
    // A list of all CardView UI elements
    private val cardViews = mutableListOf<CardView>()

    // Game state variables
    private var firstSelectedCard: MemoryCard? = null
    private var firstSelectedCardView: CardView? = null
    private var isChecking = false // True when we're checking a pair
    private var pairsFound = 0
    private var totalPairs = 0

    // Scoring variables
    private var score = 0
    private var highScore = 0
    private lateinit var sharedPrefs: SharedPreferences
    private val PREFS_NAME = "MemoryMatchPrefs"
    private val HIGH_SCORE_KEY = "HighScore"

    // Enum to define difficulty levels
    enum class Difficulty(val pairs: Int, val cols: Int, val rows: Int) {
        EASY(6, 3, 4),
        MEDIUM(8, 4, 4),
        HARD(10, 4, 5)
    }

    // Data class to represent a single card
    data class MemoryCard(val id: Int, val emoji: String, var isFaceUp: Boolean = false, var isMatched: Boolean = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view directly from the layout resource
        setContentView(R.layout.activity_game_memorymatch)

        // Initialize views using findViewById
        difficultySelector = findViewById(R.id.difficultySelector)
        gameGrid = findViewById(R.id.gameGrid)
        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)

        // --- New Score Views ---
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)

        // --- Load High Score ---
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        highScore = sharedPrefs.getInt(HIGH_SCORE_KEY, 0)
        updateHighScoreText()
        updateScoreText()

        // Set listeners for difficulty buttons
        btnEasy.setOnClickListener { setupGame(Difficulty.EASY) }
        btnMedium.setOnClickListener { setupGame(Difficulty.MEDIUM) }
        btnHard.setOnClickListener { setupGame(Difficulty.HARD) }
    }

    private fun setupGame(difficulty: Difficulty) {
        // Reset game state
        pairsFound = 0
        totalPairs = difficulty.pairs
        firstSelectedCard = null
        firstSelectedCardView = null
        isChecking = false
        cards.clear()
        cardViews.clear()
        gameGrid.removeAllViews()

        // Reset score
        score = 0
        updateScoreText()

        // Configure the GridLayout
        gameGrid.columnCount = difficulty.cols
        gameGrid.rowCount = difficulty.rows

        // Get the list of emojis for this difficulty
        val emojis = getEmojisForGame(difficulty.pairs)
        // Create pairs of cards
        for (i in 0 until difficulty.pairs) {
            cards.add(MemoryCard(i * 2, emojis[i]))
            cards.add(MemoryCard(i * 2 + 1, emojis[i]))
        }
        cards.shuffle()

        // Inflate and add card views to the grid
        for (card in cards) {
            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.list_item_card, gameGrid, false) as CardView

            // Set layout parameters to make cards fill the grid
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            // Use weights to distribute space evenly
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 8, 8, 8)
            cardView.layoutParams = params

            // Store the card data in the view's tag
            cardView.tag = card
            cardView.setOnClickListener(::onCardClicked)

            gameGrid.addView(cardView)
            cardViews.add(cardView) // Keep track of the view
        }

        // Show the game board and hide the difficulty selector
        difficultySelector.visibility = View.GONE
        gameGrid.visibility = View.VISIBLE
    }

    private fun onCardClicked(view: View) {
        if (isChecking) return // Don't allow clicks while checking a pair

        val clickedCardView = view as CardView
        val clickedCard = clickedCardView.tag as MemoryCard

        // Ignore clicks on already matched or already flipped cards
        if (clickedCard.isMatched || clickedCard.isFaceUp) return

        // Flip the card
        flipCard(clickedCardView, clickedCard, true)

        if (firstSelectedCard == null) {
            // This is the first card of the pair
            firstSelectedCard = clickedCard
            firstSelectedCardView = clickedCardView
        } else {
            // This is the second card, check for a match
            isChecking = true
            checkForMatch(clickedCard, clickedCardView)
        }
    }

    private fun checkForMatch(secondCard: MemoryCard, secondCardView: CardView) {
        val firstCard = firstSelectedCard!!
        val firstView = firstSelectedCardView!!

        if (firstCard.emoji == secondCard.emoji) {
            // It's a match!
            firstCard.isMatched = true
            secondCard.isMatched = true
            pairsFound++

            // --- Update Score ---
            score += 10
            updateScoreText()

            // Disable matched cards
            firstView.isClickable = false
            secondCardView.isClickable = false
            // Optionally change color to show they are matched
            // Updated to use ContextCompat.getColor()
            firstView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            secondCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))


            resetSelections()
            isChecking = false

            // Check for win
            if (pairsFound == totalPairs) {
                checkHighScore()
                showGameWonDialog()
            }
        } else {
            // Not a match, flip back after a delay

            // --- Update Score ---
            score = (score - 1).coerceAtLeast(0) // Don't go below 0
            updateScoreText()

            Handler(Looper.getMainLooper()).postDelayed({
                flipCard(firstView, firstCard, false)
                flipCard(secondCardView, secondCard, false)
                resetSelections()
                isChecking = false
            }, 1000) // 1 second delay
        }
    }

    // Helper function to flip a card UI
    private fun flipCard(cardView: CardView, card: MemoryCard, isFaceUp: Boolean) {
        card.isFaceUp = isFaceUp
        val cardText = cardView.findViewById<TextView>(R.id.cardEmoji)
        if (isFaceUp) {
            cardText.text = card.emoji
            // Updated to use ContextCompat.getColor()
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            cardText.text = "❓"
            // Updated to use ContextCompat.getColor()
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_secondary_variant))
        }
    }

    private fun resetSelections() {
        firstSelectedCard = null
        firstSelectedCardView = null
    }

    // --- New Scoring Functions ---

    private fun updateScoreText() {
        tvScore.text = "Score: $score"
    }

    private fun updateHighScoreText() {
        tvHighScore.text = "High Score: $highScore"
    }

    private fun checkHighScore() {
        if (score > highScore) {
            highScore = score
            updateHighScoreText()
            // Save new high score
            with(sharedPrefs.edit()) {
                putInt(HIGH_SCORE_KEY, highScore)
                apply()
            }
        }
    }

    // --- End New Scoring Functions ---


    private fun showGameWonDialog() {
        val message = "Congratulations! You found all the pairs.\nYour Score: $score"
        AlertDialog.Builder(this)
            .setTitle("You Won!")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                // Go back to difficulty selection
                gameGrid.visibility = View.GONE
                difficultySelector.visibility = View.VISIBLE
            }
            .setCancelable(false)
            .show()
    }

    // Provides the list of emojis
    private fun getEmojisForGame(numPairs: Int): List<String> {
        val allEmojis = listOf(
            "😀", "🥳", "😎", "🚀", "💡", "🎉", "🐱", "🐶", "🍕", "🍔",
            "🌟", "❤️", "🦊", "🤖", "🦄", "🍀", "💎", "🏀", "🌍", "🎁"
        )
        // Ensure we have enough emojis, then take the required number
        return allEmojis.shuffled().take(numPairs)
    }
}
