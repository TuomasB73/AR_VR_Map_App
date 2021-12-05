package com.virtualmapdevs.ar_vr_map

import android.app.Activity
import android.content.Context

object Animatoo {

    fun animateSlideLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_left_enter,
            R.anim.animate_slide_left_exit
        )
    }

}