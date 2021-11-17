package com.virtualmapdevs.ar_vr_map

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.virtualmapdevs.ar_vr_map.fragments.HomeFragment
import com.virtualmapdevs.ar_vr_map.fragments.LoginFragment
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val sharedPrefFile = "loginsharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkIsUserLoggedIn()
    }

    private fun checkIsUserLoggedIn() {
        val sharedPreference = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val loginId = sharedPreference?.getString("loginKey", "")

        Log.d("checkIsUserLoggedIn test", "loginId: $loginId")

        if (loginId != "" && loginId != null) {
            checkSecureData(loginId)
        } else {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
            }
        }
    }

    private fun checkSecureData(token: String) {
        viewModel.getSecureData("Bearer $token")

        viewModel.secureDataMsg.observe(this, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "loginUserMsg: ${response.body()}")
                Log.d("artest", "loginUserMsg: ${response.code()}")

                Log.d("artest", "Token ok")
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
}