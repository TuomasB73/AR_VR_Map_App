package com.virtualmapdevs.ar_vr_map

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.arModeButton).setOnClickListener {
            val arModeIntent = Intent(this, ARModeActivity::class.java)
            startActivity(arModeIntent)
        }
    }
}