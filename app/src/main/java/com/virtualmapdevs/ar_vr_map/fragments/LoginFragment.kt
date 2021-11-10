package com.virtualmapdevs.ar_vr_map.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class LoginFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private val sharedPrefFile = "loginsharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIsUserLoggedIn()

        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            val usernameTxt = view.findViewById<EditText>(R.id.usernameAdd).text.toString()
            val passwordTxt = view.findViewById<EditText>(R.id.passwordAdd).text.toString()
            viewModel.loginUser(usernameTxt, passwordTxt)
        }

        viewModel.loginUserMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "loginUserMsg: ${response.body()}")
                Log.d("artest", "loginUserMsg: ${response.code()}")

                val loginToken = response.body()?.message

                val sharedPreference =
                    activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

                val editor = sharedPreference?.edit()
                editor?.putString("loginKey", loginToken)
                editor?.apply()

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<HomeFragment>(R.id.fragmentContainer)
                    addToBackStack(null)
                }

            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loginUserMessageFail.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })

        view.findViewById<Button>(R.id.regBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<RegistrationFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }
    }

    private fun checkIsUserLoggedIn() {

        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val loginId = sharedPreference?.getString("loginKey", "")

        Log.d("checkIsUserLoggedIn test", "loginId: $loginId")

/*        if (loginId != null) {
            checkSecureData(loginId)
        }*/

        if (loginId != "") {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<HomeFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }
    }

/*    private fun checkSecureData(token: String) {

        viewModel.getSecureData(token)

        viewModel.secureDataMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "loginUserMsg: ${response.body()}")
                Log.d("artest", "loginUserMsg: ${response.code()}")

                Log.d("artest", "Token ok")
            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })
    }*/
}