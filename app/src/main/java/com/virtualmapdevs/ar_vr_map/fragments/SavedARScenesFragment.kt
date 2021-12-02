package com.virtualmapdevs.ar_vr_map.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.SavedItemAdapter
import com.virtualmapdevs.ar_vr_map.databinding.FragmentSavedARScenesBinding
import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem

class SavedARScenesFragment : Fragment(), SavedItemAdapter.ClickListener {
    private var userToken: String? = null
    private val viewModel: MainViewModel by viewModels()
    private var maps: List<ARItem> = arrayListOf()
    private var matchedMaps: List<ARItem> = arrayListOf()
    private lateinit var savedItemAdapter: SavedItemAdapter
    private lateinit var binding: FragmentSavedARScenesBinding
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

                savedItemAdapter = SavedItemAdapter(maps.toMutableList(), this, requireContext())
                binding.savedItemsRecyclerView.adapter = savedItemAdapter
                binding.searchView.isSubmitButtonEnabled = true
            }
        })
    }

    override fun onItemClick(arItemId: String?) {
        mapActionsDialog(arItemId)
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


    private fun mapActionsDialog(arItemId: String?) {

        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.map_actions_dialog)

        val descriptionText = dialog.findViewById(R.id.mapDescriptionTextView) as TextView
        val deleteBtn = dialog.findViewById(R.id.deleteSavedItemButton) as Button
        val openArBtn = dialog.findViewById(R.id.openARbtn) as Button
        val showInMapBtn = dialog.findViewById(R.id.openInMapBtn) as Button
        val cancelBtn = dialog.findViewById(R.id.cancelButton) as Button

        //descriptionText.text = savedItemAdapter.getItem(arItemId)?.description

        openArBtn.setOnClickListener {
            dialog.dismiss()
            val bundle = bundleOf("arItemId" to arItemId)

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<ArModeFragment>(R.id.fragmentContainer, args = bundle)
                addToBackStack(null)
            }
        }

        showInMapBtn.setOnClickListener {
            locationMapDialog()
            dialog.dismiss()
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

    private fun locationMapDialog() {

        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.location_map_dialog)

        val cancelBtn = dialog.findViewById(R.id.lMcancelBtn) as Button
        val map = dialog.findViewById(R.id.dialogMapView) as MapView

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        map.setTileSource(TileSourceFactory.MAPNIK) //render
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(12.0)
        val startPoint = GeoPoint(60.224305, 24.757239)
        mapController.setCenter(startPoint)


/*        marker = Marker(map)
        marker.icon = AppCompatResources.getDrawable(this.requireContext(), R.drawable.ic_pin)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.textLabelFontSize = 20*/

        val items = java.util.ArrayList<OverlayItem>()
        items.add(OverlayItem("Espoo", "Karaportti", GeoPoint(60.224305, 24.757239)))

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