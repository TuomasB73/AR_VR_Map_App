package com.virtualmapdevs.ar_vr_map.fragments

import android.app.AlertDialog
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
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class LoginFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            val usernameTxt = view.findViewById<EditText>(R.id.usernameAdd).text.toString()
            val passwordTxt = view.findViewById<EditText>(R.id.passwordAdd).text.toString()
            viewModel.loginUser(usernameTxt, passwordTxt)
        }

        view.findViewById<Button>(R.id.infoButton).setOnClickListener {
            val builder = AlertDialog.Builder(this.requireContext())
            builder.setTitle("Username and password")
            builder.setMessage("Username must be 3 or more characters and start with capital letter" +
                    "\n" + "\n" +
                    "Password must be at least 8 characters in length and have 1 capital letter")

            builder.setPositiveButton("Ok") { _, _ -> }
            builder.show()
        }

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

        view.findViewById<Button>(R.id.regBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<RegistrationFragment>(R.id.fragmentContainer)
            }
        }
    }
}