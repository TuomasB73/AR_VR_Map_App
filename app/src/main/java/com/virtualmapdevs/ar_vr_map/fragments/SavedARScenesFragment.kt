package com.virtualmapdevs.ar_vr_map.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.virtualmapdevs.ar_vr_map.SavedItemAdapter
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class SavedARScenesFragment : Fragment(), SavedItemAdapter.ClickListener {
    private val sharedPrefFile = "loginsharedpreference"
    private var loginToken: String? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_a_r_scenes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedItemsRecyclerView = view.findViewById<RecyclerView>(R.id.savedItemsRecyclerView)
        val layoutManager = LinearLayoutManager(this.context)
        savedItemsRecyclerView.layoutManager = layoutManager

        val sharedPreference = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        loginToken = sharedPreference?.getString("loginKey", "")

        if (loginToken != null) {
            viewModel.getUserScannedItems("Bearer $loginToken")
        }

        viewModel.getUserScannedItemsMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val savedArItems = response.body()
                savedItemsRecyclerView.adapter = SavedItemAdapter(savedArItems, this)
            }
        })

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onItemClick(arItemId: String?) {
        val bundle = bundleOf("arItemId" to arItemId)

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ArModeFragment>(R.id.fragmentContainer, args = bundle)
            addToBackStack(null)
        }
    }
}