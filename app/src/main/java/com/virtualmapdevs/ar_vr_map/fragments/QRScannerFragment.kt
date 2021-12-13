package com.virtualmapdevs.ar_vr_map.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.budiyev.android.codescanner.*
import com.google.zxing.Result
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.utils.NetworkVariables
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class QRScannerFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        // Open Street Map API registering
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        lifecycleScope.launch {
            if (NetworkVariables.isNetworkConnected) {
                checkPermissions()
                startScanning()
            } else {
                showNoConnectionDialog()
            }
        }
    }

    // This will show dialog if internet connection is not found
    private fun showNoConnectionDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("No connection")
        builder.setMessage("Check your Internet connection and try again")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Test connection") { _, _ ->
            if (NetworkVariables.isNetworkConnected) {
                checkPermissions()
                startScanning()
            } else {
                showNoConnectionDialog()
            }
        }
        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun startScanning() {
        // Parameters (default values)
        val scannerView = view?.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = scannerView?.let { context?.let { it1 -> CodeScanner(it1, it) } }!!
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                isQrCodeValidCheck(it)
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            activity?.runOnUiThread {

            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    // Asks permission to use camera
    private fun checkPermissions() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA),
                    0
                )
            }
        }
    }
    
    // This will check if the qr code is valid
    private fun isQrCodeValidCheck(result: Result) {
        val inTest = result.toString()
        if (inTest.length == 24) {
            if (isLettersOrNumbers(inTest)) {
                fetchQRItemData(inTest)
            } else {
                Toast.makeText(activity, "Not a valid QR code", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(activity, "Not a valid QR code", Toast.LENGTH_LONG).show()
        }
    }

    // Valid qr code contain only letters and numbers
    private fun isLettersOrNumbers(string: String): Boolean {
        for (c in string) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

    // Fetch data for the dialog description and location
    private fun fetchQRItemData(arItemId: String?) {
        val userToken = SharedPreferencesFunctions.getUserToken(requireActivity())
        if (arItemId != null) {
            viewModel.getArItemById(userToken, arItemId)
        }

        viewModel.arItembyIdMsg.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val itemTitle = response.body()?.name
                val itemDescription = response.body()?.description
                val latitude = response.body()?.latitude
                val longitude = response.body()?.longitude

                if (itemTitle != null && itemDescription != null && latitude != null && longitude != null) {

                    mapActionsDialog(arItemId, itemDescription, latitude, longitude)
                } else {
                    Log.d("ARItemFetch", "Item title and/or description not found")
                    Toast.makeText(
                        activity,
                        "Item title and/or description not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // This will open dialog that show description of the 3D map and two buttons
    // to open 3D map in AR mode or show location in 2D map
    private fun mapActionsDialog(
        arItemId: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.map_actions_dialog_qr)

        val descriptionText = dialog.findViewById(R.id.mapDescriptionTextView) as TextView
        val openArBtn = dialog.findViewById(R.id.openARbtn) as Button
        val showInMapBtn = dialog.findViewById(R.id.openInMapBtn) as Button
        val cancelBtn = dialog.findViewById(R.id.cancelButton) as Button

        descriptionText.text = description

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
            locationMapDialog(latitude, longitude)
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()

            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<HomeFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }

        dialog.show()
    }

    // This open dialog that show 3D map location in 2D map
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