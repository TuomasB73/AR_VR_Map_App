package com.virtualmapdevs.ar_vr_map.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Starts the network callback for testing connection
        NetworkMonitor(application).startNetworkCallback()

        // If there's no internet connection, an error dialog is shown to the user
        lifecycleScope.launch {
            if (NetworkVariables.isNetworkConnected) {
                checkIsUserLoggedIn()
            } else {
                showNoConnectionDialog()
            }
        }

        // Checks if the onboarding is already shown. If not it will be shown
        if (SharedPreferencesFunctions.isOnboardingShownCheck(this) == "no") {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }
    }

    // Creates a no connection dialog with a retest option
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

    /* Gets the saved user token from the SharedPreferences if it exists and makes a server request
    for authentication. If no token is saved, the app proceeds to the login fragment */
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

    /* Makes a server check request to confirm authentication and then moves to home fragment. If token
    is not valid the app moves to login fragment */
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
        /* If the user has navigated to the AR mode fragment through the QR scanner fragment and presses
        the back button, they will be transitioned back to the home fragment skipping the QR scanner fragment */
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