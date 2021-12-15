package com.virtualmapdevs.area_explorer.utils

import android.app.Activity
import android.content.Context
import android.util.Log

// Singleton object for all the SharedPreference functions
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
        val user = userToken!!.substring(0, 100)
        val editor = sharedPreference.edit()
        editor?.putString("video$user", "yes")
        editor?.apply()
    }

    fun resetVideoShown(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        val user = userToken!!.substring(0, 100)
        val editor = sharedPreference?.edit()
        editor?.putString("video$user", "no")
        editor?.apply()
    }

    fun isVideoShownCheck(activity: Activity): String? {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        val user = userToken!!.substring(0, 100)
        Log.d("sharedtest", "isVideoShown: $user")
        return sharedPreference?.getString("video$user", "no")
    }

    fun saveOnboardingShown(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        var user = "Bearer "
        if (userToken != "Bearer ") {
            user = userToken!!.substring(0, 100)
        }
        val editor = sharedPreference.edit()
        Log.d("sharedtest", "saveOnboardingShown: $user")
        editor?.putString("onboarding$user", "yes")
        editor?.apply()
    }

    fun isOnboardingShownCheck(activity: Activity): String? {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        userToken = getUserToken(activity)
        var user = "Bearer "
        if (userToken != "Bearer ") {
            user = userToken!!.substring(0, 100)
            Log.d("sharedtest", "userToken $userToken")
        }
        Log.d("sharedtest", "isOnboardingShownCheck: $user")
        return sharedPreference?.getString("onboarding$user", "no")
    }

    fun clearEmptyToken(activity: Activity) {
        val sharedPreference = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.remove("onboardingBearer ")
        editor.apply()
    }
}