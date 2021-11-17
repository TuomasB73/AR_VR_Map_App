package com.virtualmapdevs.ar_vr_map.utils

import android.app.Activity
import android.content.Context

object SharedPreferencesFunctions {
    private const val sharedPrefFile = "userSharedPreferences"

    fun getUserToken(activity: Activity): String {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        return "Bearer ${sharedPreference.getString("userToken", "")}"
    }

    fun saveUserToken(activity: Activity, userToken: String) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("userToken", userToken)
        editor?.apply()
    }

    fun removeUserToken(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.remove("userToken")
        editor.apply()
    }
}