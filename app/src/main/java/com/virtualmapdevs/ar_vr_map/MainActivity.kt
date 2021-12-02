package com.virtualmapdevs.ar_vr_map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.virtualmapdevs.ar_vr_map.fragments.HomeFragment
import com.virtualmapdevs.ar_vr_map.fragments.LoginFragment
import com.virtualmapdevs.ar_vr_map.utils.NetworkMonitor
import com.virtualmapdevs.ar_vr_map.utils.NetworkVariables
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*NetworkMonitor(application).startNetworkCallback()

        if (NetworkVariables.isNetworkConnected) {
            checkIsUserLoggedIn()
        } else {
            findViewById<TextView>(R.id.networkErrorMessageTextView).visibility = View.VISIBLE
        }*/

        checkIsUserLoggedIn()
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
            supportFragmentManager.getBackStackEntryAt(0).name == "QRScannerFragment") {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
        } else {
            super.onBackPressed()
        }
    }
}