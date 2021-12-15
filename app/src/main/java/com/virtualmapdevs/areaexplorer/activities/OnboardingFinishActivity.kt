package com.virtualmapdevs.areaexplorer.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.virtualmapdevs.areaexplorer.R
import com.virtualmapdevs.areaexplorer.utils.SharedPreferencesFunctions

class OnboardingFinishActivity : AppCompatActivity() {
    private lateinit var btnStart: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_finish)
        btnStart = findViewById(R.id.layout_start)
        btnStart.setOnClickListener {
            // This will save that user have now seen onboarding
            SharedPreferencesFunctions.saveOnboardingShown(this)
            finish()
        }
    }
}