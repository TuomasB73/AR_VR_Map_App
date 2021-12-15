package com.virtualmapdevs.area_explorer.fragments

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
import com.virtualmapdevs.area_explorer.R
import com.virtualmapdevs.area_explorer.utils.SharedPreferencesFunctions
import com.virtualmapdevs.area_explorer.viewmodels.MainViewModel

class RegistrationFragment : Fragment() {
    private var usernameTxt: String? = null
    private var passwordTxt: String? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Makes a registration request to the server
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            usernameTxt = view.findViewById<EditText>(R.id.registerUsernameAdd).text.toString()
            passwordTxt = view.findViewById<EditText>(R.id.registerPasswordAdd).text.toString()
            val passwordConfirmTxt =
                view.findViewById<EditText>(R.id.registerPasswordConfirmAdd).text.toString()

            if (usernameTxt != null && passwordTxt != null) {
                if (passwordTxt == passwordConfirmTxt) {
                    viewModel.registerUser(usernameTxt!!, passwordTxt!!)
                } else {
                    Toast.makeText(activity, "Check your passwords", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Opens a info dialog of the username and password requirements
        view.findViewById<Button>(R.id.infoRegisterButton).setOnClickListener {
            instructionDialog()
        }

        // If the registration was successful a login request is made
        viewModel.registerUserMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "postUserMsg: ${response.body()}")
                Log.d("artest", "postUserMsg: ${response.code()}")

                if (usernameTxt != null && passwordTxt != null) {
                    viewModel.loginUser(usernameTxt!!, passwordTxt!!)
                }
            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.registerUserMsgFail.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })

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

        // The app changes to the login fragment
        view.findViewById<Button>(R.id.logBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
            }
        }
    }

    // Dialog that shows user instructions how to create acceptable username and password
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