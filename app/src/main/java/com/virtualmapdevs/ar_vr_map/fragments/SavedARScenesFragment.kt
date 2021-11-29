package com.virtualmapdevs.ar_vr_map.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.SavedItemAdapter
import com.virtualmapdevs.ar_vr_map.databinding.FragmentSavedARScenesBinding
import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class SavedARScenesFragment : Fragment(), SavedItemAdapter.ClickListener {
    private var userToken: String? = null
    private val viewModel: MainViewModel by viewModels()
    private var maps: List<ARItem> = arrayListOf()
    private var matchedMaps: List<ARItem> = arrayListOf()
    private lateinit var savedItemAdapter: SavedItemAdapter
    private lateinit var binding: FragmentSavedARScenesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSavedARScenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(this.context, 2)
        binding.savedItemsRecyclerView.layoutManager = layoutManager

        userToken = SharedPreferencesFunctions.getUserToken(requireActivity())

        fetchSavedItemsAndSetAdapter()
        performSearch()

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun fetchSavedItemsAndSetAdapter() {
        if (userToken != null) {
            viewModel.getUserScannedItems(userToken!!)
        }

        viewModel.getUserScannedItemsMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val savedArItems = response.body()?.reversed()

                if (savedArItems != null) {
                    maps = savedArItems
                }

                savedItemAdapter = SavedItemAdapter(maps.toMutableList(), this)
                binding.savedItemsRecyclerView.adapter = savedItemAdapter
                binding.searchView.isSubmitButtonEnabled = true
            }
        })
    }

    override fun onItemClick(arItemId: String?) {
        val bundle = bundleOf("arItemId" to arItemId)

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ArModeFragment>(R.id.fragmentContainer, args = bundle)
            addToBackStack(null)
        }
    }

    override fun onDeleteButtonPressed(arItemId: String?) {

        val builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("DELETE")
        builder.setMessage("Are you sure you want to delete map?")

        builder.setPositiveButton("Ok") { _, _ ->
            if (userToken != null && arItemId != null) {
                viewModel.deleteUserScannedItem(userToken!!, arItemId)

                viewModel.deleteUserScannedItemMsg.observe(viewLifecycleOwner, { response ->
                    if (response.isSuccessful) {
                        val message = response.body()?.message
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        fetchSavedItemsAndSetAdapter()
                    } else {
                        Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
                    }
                })

                viewModel.deleteUserScannedItemMsgFail.observe(viewLifecycleOwner, {
                    Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                })
            }
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(
                activity,
                "cancelled", Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }

    override fun onResume() {
        fetchSavedItemsAndSetAdapter()
        super.onResume()
    }

    private fun performSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })
    }

    private fun search(text: String?) {
        matchedMaps = arrayListOf()

        text?.let {
            maps.forEach { mapName ->
                if (mapName.name.contains(text, true)
                ) {
                    (matchedMaps as ArrayList<ARItem>).add(mapName)
                }
            }
            if (matchedMaps.isEmpty()) {
                Toast.makeText(activity, "No match found!", Toast.LENGTH_SHORT).show()
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        savedItemAdapter.updateData(matchedMaps)
    }
}