package com.virtualmapdevs.ar_vr_map

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import kotlinx.android.synthetic.main.activity_onboarding_finish.*

class OnboardingFinishActivity : AppCompatActivity() {
    private lateinit var btnStart: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_finish)
        btnStart = layout_start
        btnStart.setOnClickListener {
            // This will save that user have now seen onboarding
            SharedPreferencesFunctions.saveOnboardingShown(this)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
