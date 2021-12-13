package com.virtualmapdevs.ar_vr_map.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions

class HomeFragment : Fragment() {

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

        // This will use user token to save that user have seen onboarding
        SharedPreferencesFunctions.saveOnboardingShown(requireActivity())
        // This will clear "empty token" that was used when user was not yet registered
        SharedPreferencesFunctions.clearEmptyToken(requireActivity())

        view.findViewById<Button>(R.id.readQrCodeButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<QRScannerFragment>(R.id.fragmentContainer)
                /* The QR scanner fragment name is attached in the backstack entry so it can be identified
                later in the back button navigation logic */
                addToBackStack("QRScannerFragment")
            }
        }

        view.findViewById<Button>(R.id.mySavedMapsButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<SavedARScenesFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

        view.findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            SharedPreferencesFunctions.removeUserToken(requireActivity())

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
            }
        }
    }
}