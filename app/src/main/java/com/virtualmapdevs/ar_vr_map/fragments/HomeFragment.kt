package com.virtualmapdevs.ar_vr_map.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.virtualmapdevs.ar_vr_map.R

class HomeFragment : Fragment() {

    private val sharedPrefFile = "loginsharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val loginId = sharedPreference?.getString("loginKey", "defaultValue")

        Log.d("artest", "loginId: $loginId")

        view.findViewById<Button>(R.id.readQrCodeButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<QRScannerFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

        view.findViewById<Button>(R.id.savedScenesButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<SavedARScenesFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

        view.findViewById<Button>(R.id.arModeButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<ArModeFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

        view.findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            val editor = sharedPreference?.edit()
            editor?.putString("loginKey", "")
            editor?.apply()

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
            }

            // for testing only
            val logoutTest = sharedPreference?.getString("loginKey", "defaultValue")
            Log.d("artest", "loginId: $logoutTest")
        }
    }
}