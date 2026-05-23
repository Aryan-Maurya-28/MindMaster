package com.example.mindmaster // Make sure this matches your package name

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Get references to all 6 game icons
        val gameIcon1 = findViewById<CardView>(R.id.gameIcon1)
        val gameIcon2 = findViewById<CardView>(R.id.gameIcon2)
        val gameIcon3 = findViewById<CardView>(R.id.gameIcon3)
        val gameIcon4 = findViewById<CardView>(R.id.gameIcon4)
        val gameIcon5 = findViewById<CardView>(R.id.gameIcon5)
        val gameIcon6 = findViewById<CardView>(R.id.gameIcon6)
        val gameIcon7 = findViewById<CardView>(R.id.gameIcon7)
        val gameIcon8 = findViewById<CardView>(R.id.gameIcon8)
        val gameIcon9 = findViewById<CardView>(R.id.gameIcon9)
        val gameIcon10 = findViewById<CardView>(R.id.gameIcon10)// Set click listeners for each icon
        gameIcon1.setOnClickListener {
            val intent = Intent(this, Game2048Activity::class.java)
            startActivity(intent)
        }

        gameIcon2.setOnClickListener {
            val intent = Intent(this, MemoryMatchActivity::class.java)
            startActivity(intent)
        }

        gameIcon3.setOnClickListener {
            val intent = Intent(this, JigsawPuzzleActivity::class.java)
            startActivity(intent)
        }

        gameIcon4.setOnClickListener {
            val intent = Intent(this, WaterSortActivity::class.java)
            startActivity(intent)
        }

        gameIcon5.setOnClickListener {
            val intent = Intent(this, SlidePuzzleActivity::class.java)
            startActivity(intent)
        }

        gameIcon6.setOnClickListener {
            val intent = Intent(this, SudokuActivity::class.java)
            startActivity(intent)
        }

        gameIcon7.setOnClickListener {
            val intent = Intent(this, MathQuizActivity::class.java)
            startActivity(intent)
        }

        gameIcon8.setOnClickListener {
            val intent = Intent(this, WordCrossActivity::class.java)
            startActivity(intent)
        }

        gameIcon9.setOnClickListener {
            val intent = Intent(this, GeneralKnowledgeActivity::class.java)
            startActivity(intent)
        }

        gameIcon10.setOnClickListener {
            val intent = Intent(this, OddManOutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showGameToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}