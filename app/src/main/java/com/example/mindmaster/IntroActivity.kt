package com.example.mindmaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private var isSkipped = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        val videoView: VideoView = findViewById(R.id.splashVideo)


        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                skipVideo()
                return true
            }
        })

        val videoPath = "android.resource://$packageName/${R.raw.intro_video}"
        val uri = Uri.parse(videoPath)
        videoView.setVideoURI(uri)


        videoView.start()


        videoView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        videoView.setOnCompletionListener {
            skipVideo()
        }
    }


    private fun skipVideo() {
        if (isSkipped) return
        isSkipped = true

        val videoView: VideoView = findViewById(R.id.splashVideo)
        if (videoView.isPlaying) {
            videoView.stopPlayback()
        }
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

}
