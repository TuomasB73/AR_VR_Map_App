package com.virtualmapdevs.ar_vr_map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegistrationFragment : Fragment() {

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

        val usernameTxt = view.findViewById<EditText>(R.id.registerUsernameAdd)
        val passwordTxt = view.findViewById<EditText>(R.id.registerPasswordAdd)
        val passwordConfirmTxt = view.findViewById<EditText>(R.id.registerPasswordConfirmAdd)

        if (passwordTxt == passwordConfirmTxt){

        } else {
            Toast.makeText(activity, "Check your passwords", Toast.LENGTH_LONG).show()
        }

        view.findViewById<Button>(R.id.registerButton).setOnClickListener{

        }
    }
}