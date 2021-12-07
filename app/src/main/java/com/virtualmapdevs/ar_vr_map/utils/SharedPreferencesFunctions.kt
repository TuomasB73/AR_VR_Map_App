package com.virtualmapdevs.ar_vr_map.utils

import android.app.Activity
import android.content.Context

object SharedPreferencesFunctions {
    private const val sharedPrefFile = "userSharedPreferences"
    private var userToken: String? = null

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

    fun saveVideoShown(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        val editor = sharedPreference.edit()
        editor?.putString("video"+userToken.toString(), "yes")
        editor?.apply()
    }

    fun resetVideoShown(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        val editor = sharedPreference?.edit()
        editor?.putString("video"+userToken.toString(), "no")
        editor?.apply()
    }

    fun isVideoShownCheck(activity: Activity): String? {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        return sharedPreference?.getString("video"+userToken.toString(), "no")
    }

    fun saveOnboardingShown(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        val editor = sharedPreference.edit()
        editor?.putString("onboarding"+userToken.toString(), "yes")
        editor?.apply()
    }

    fun isOnboardingShownCheck(activity: Activity): String? {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        return sharedPreference?.getString("onboarding"+userToken.toString(), "no")
    }
}