package com.virtualmapdevs.area_explorer.utils

import android.util.Log
import kotlin.properties.Delegates

/**
 * Network status available to anywhere in the application
 */
object NetworkVariables {
    var isNetworkConnected: Boolean by Delegates.observable(false) { _, _, newValue ->
        Log.d("networktest", "new value: $newValue")
    }
}