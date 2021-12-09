package com.virtualmapdevs.ar_vr_map.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.virtualmapdevs.ar_vr_map.R

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val videoView : VideoView = findViewById(R.id.splahVideoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.splashlogo}")

        videoView.setVideoURI(offlineUri)
        videoView.requestFocus()
        videoView.start()
        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // is the delayed time in milliseconds.
    }
}