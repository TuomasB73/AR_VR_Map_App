package com.virtualmapdevs.ar_vr_map.fragments

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
import androidx.lifecycle.LifecycleOwner
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class RegistrationFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

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
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


/*        val passwordConfirmTxt = view.findViewById<EditText>(R.id.registerPasswordConfirmAdd)

        if (passwordTxt == passwordConfirmTxt){

        } else {
            Toast.makeText(activity, "Check your passwords", Toast.LENGTH_LONG).show()
        }*/

        view.findViewById<Button>(R.id.registerButton).setOnClickListener{
            val usernameTxt = view.findViewById<EditText>(R.id.registerUsernameAdd).text.toString()
            val passwordTxt = view.findViewById<EditText>(R.id.registerPasswordAdd).text.toString()
            viewModel.registerUser(usernameTxt, passwordTxt)
        }

        viewModel.registerUserMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "postUserMsg: ${response.body()}")
                Log.d("artest", "postUserMsg: ${response.code()}")
            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })

        view.findViewById<Button>(R.id.logBtn).setOnClickListener{
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoginFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

/*        viewModel.getMessage()
        viewModel.getMessageMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "getmessaMsg: ${response.body()}")
                Log.d("artest", "getmessaMsg: ${response.code()}")
            } else {
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })*/



    }
}