package com.virtualmapdevs.areaexplorer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.virtualmapdevs.areaexplorer.R
import com.virtualmapdevs.areaexplorer.adapters.SavedItemAdapter
import com.virtualmapdevs.areaexplorer.databinding.FragmentSavedARScenesBinding
import com.virtualmapdevs.areaexplorer.model.ARItem
import com.virtualmapdevs.areaexplorer.utils.NetworkVariables
import com.virtualmapdevs.areaexplorer.utils.SharedPreferencesFunctions
import com.virtualmapdevs.areaexplorer.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class SavedARScenesFragment : Fragment(), SavedItemAdapter.ClickListener {
    private var userToken: String? = null
    private val viewModel: MainViewModel by viewModels()
    private var maps: List<ARItem> = arrayListOf()
    private var matchedMaps: List<ARItem> = arrayListOf()
    private lateinit var savedItemAdapter: SavedItemAdapter
    private lateinit var binding: FragmentSavedARScenesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedARScenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open Street Map API registering
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        val layoutManager = GridLayoutManager(this.context, 2)
        binding.savedItemsRecyclerView.layoutManager = layoutManager

        userToken = SharedPreferencesFunctions.getUserToken(requireActivity())

        // If there's no internet connection, an error dialog is shown to the user
        lifecycleScope.launch {
            if (NetworkVariables.isNetworkConnected) {
                fetchSavedItemsAndSetAdapter()
                performSearch()
            } else {
                showNoConnectionDialog()
            }
        }

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    // Creates a no connection dialog with a retest option
    private fun showNoConnectionDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("No connection")
        builder.setMessage("Check your Internet connection and try again")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Test connection") { _, _ ->
            if (NetworkVariables.isNetworkConnected) {
                fetchSavedItemsAndSetAdapter()
                performSearch()
            } else {
                showNoConnectionDialog()
            }
        }
        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
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

                savedItemAdapter = SavedItemAdapter(maps.toMutableList(), this, requireContext())
                binding.savedItemsRecyclerView.adapter = savedItemAdapter
                binding.searchView.isSubmitButtonEnabled = true
                binding.itemsListLoadingIndicator.visibility = View.GONE
            }
        })
    }

    override fun onItemClick(
        arItemId: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        mapActionsDialog(arItemId, description, latitude, longitude)
    }

    // Creates a delete confirmation dialog
    private fun onDeleteButtonPressed(arItemId: String?) {

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

    // Compares the search text to the names of the maps in the list and updates the recyclerView
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

    // Creates a dialog for the maps with ar mode, show map and delete options
    private fun mapActionsDialog(
        arItemId: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?
    ) {

        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.map_actions_dialog)

        val descriptionText = dialog.findViewById(R.id.mapDescriptionTextView) as TextView
        val deleteBtn = dialog.findViewById(R.id.deleteSavedItemButton) as Button
        val openArBtn = dialog.findViewById(R.id.openARbtn) as Button
        val showInMapBtn = dialog.findViewById(R.id.openInMapBtn) as Button
        val cancelBtn = dialog.findViewById(R.id.cancelButton) as Button

        descriptionText.text = description

        openArBtn.setOnClickListener {
            dialog.dismiss()
            // The AR item ID is passed to the ar mode fragment
            val bundle = bundleOf("arItemId" to arItemId)

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<ArModeFragment>(R.id.fragmentContainer, args = bundle)
                addToBackStack(null)
            }
        }

        showInMapBtn.setOnClickListener {
            locationMapDialog(latitude, longitude)
        }

        deleteBtn.setOnClickListener {
            onDeleteButtonPressed(arItemId)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Dialog that show 2D map and location of the 3d map object
    private fun locationMapDialog(latitude: Double?, longitude: Double?) {

        if (latitude != null && longitude != null) {

            val dialog = Dialog(this.requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.location_map_dialog)

            val cancelBtn = dialog.findViewById(R.id.lMcloseBtn) as Button
            val map = dialog.findViewById(R.id.dialogMapView) as MapView

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            map.setTileSource(TileSourceFactory.MAPNIK) //render
            map.setMultiTouchControls(true)

            val mapController = map.controller
            mapController.setZoom(12.0)
            val startPoint = GeoPoint(latitude, longitude)
            mapController.setCenter(startPoint)

            val items = java.util.ArrayList<OverlayItem>()
            items.add(OverlayItem("Title", "Snippet", GeoPoint(latitude, longitude)))

            val mOverlay = ItemizedIconOverlay(context,
                items, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                        return false
                    }
                })
            mOverlay.focus
            map.overlays.add(mOverlay)

            dialog.show()
        }
    }
}