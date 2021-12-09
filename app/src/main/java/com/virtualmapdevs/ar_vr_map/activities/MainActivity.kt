package com.virtualmapdevs.ar_vr_map.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.fragments.HomeFragment
import com.virtualmapdevs.ar_vr_map.fragments.LoginFragment
import com.virtualmapdevs.ar_vr_map.utils.NetworkMonitor
import com.virtualmapdevs.ar_vr_map.utils.NetworkVariables
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This disables dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        NetworkMonitor(application).startNetworkCallback()

        lifecycleScope.launch {
            if (NetworkVariables.isNetworkConnected) {
                checkIsUserLoggedIn()
            } else {
                showNoConnectionDialog()
            }
        }

        if (SharedPreferencesFunctions.isOnboardingShownCheck(this) == "no") {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showNoConnectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No connection")
        builder.setMessage("Check your Internet connection and try again")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Test connection") { _, _ ->
            if (NetworkVariables.isNetworkConnected) {
                checkIsUserLoggedIn()
            } else {
                Toast.makeText(applicationContext, "No connection", Toast.LENGTH_LONG).show()
                showNoConnectionDialog()
            }
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun checkIsUserLoggedIn() {
        userToken = SharedPreferencesFunctions.getUserToken(this)

        Log.d("checkIsUserLoggedIn test", "userToken: $userToken")

        if (userToken != null && userToken != "Bearer ") {
            checkSecureData(userToken!!)
        } else {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
            }
        }
    }

    private fun checkSecureData(userToken: String) {
        viewModel.getSecureData(userToken)

        viewModel.secureDataMsg.observe(this, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "loginUserMsg: ${response.body()}")
                Log.d("artest", "loginUserMsg: ${response.code()}")
                Log.d("artest", "userToken ok")

                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<HomeFragment>(R.id.fragmentContainer)
                }
            } else {
                Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()

                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<LoginFragment>(R.id.fragmentContainer)
                }
            }
        })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1 &&
            supportFragmentManager.getBackStackEntryAt(0).name == "QRScannerFragment"
        ) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
        } else {
            super.onBackPressed()
        }
    }
}