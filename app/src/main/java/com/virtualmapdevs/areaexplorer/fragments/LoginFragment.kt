package com.virtualmapdevs.areaexplorer.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.virtualmapdevs.areaexplorer.R
import com.virtualmapdevs.areaexplorer.utils.SharedPreferencesFunctions
import com.virtualmapdevs.areaexplorer.viewmodels.MainViewModel

class LoginFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Makes a login request to the server
        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            val usernameTxt = view.findViewById<EditText>(R.id.usernameAdd).text.toString()
            val passwordTxt = view.findViewById<EditText>(R.id.passwordAdd).text.toString()
            viewModel.loginUser(usernameTxt, passwordTxt)
        }

        // Opens a info dialog of the username and password requirements
        view.findViewById<Button>(R.id.infoButton).setOnClickListener {
            instructionDialog()
        }

        /* If the login was successful the user token will be saved and the app proceeds to the home
        fragment */
        viewModel.loginUserMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "loginUserMsg: ${response.body()}")
                Log.d("artest", "loginUserMsg: ${response.code()}")

                val userToken = response.body()?.message
                Log.d("userToken", userToken!!)

                SharedPreferencesFunctions.saveUserToken(requireActivity(), userToken)

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<HomeFragment>(R.id.fragmentContainer)
                }
            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loginUserMsgFail.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })

        // The app changes to the registration fragment
        view.findViewById<Button>(R.id.regBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<RegistrationFragment>(R.id.fragmentContainer)
            }
        }
    }

    // Dialog that shows user acceptable username and password
    private fun instructionDialog() {
        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.logreg_instructions_dialog)

        val okBtn = dialog.findViewById(R.id.okBtn) as Button

        okBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}