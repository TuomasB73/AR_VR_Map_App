package com.virtualmapdevs.area_explorer

import android.app.Activity
import android.content.Context

// This is used in onboarding
object Animatoo {
    fun animateSlideLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_left_enter,
            R.anim.animate_slide_left_exit
        )
    }
}