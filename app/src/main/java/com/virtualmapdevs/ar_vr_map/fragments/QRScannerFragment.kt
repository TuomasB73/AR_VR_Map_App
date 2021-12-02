package com.virtualmapdevs.ar_vr_map.fragments

import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.model.Poi
import com.virtualmapdevs.ar_vr_map.utils.Constants
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class QRScannerFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open Street Map API registering
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        checkPermissions()
        startScanning()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false)
    }

    private fun startScanning() {

        // Parameters (default values)
        val scannerView = view?.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = scannerView?.let { context?.let { it1 -> CodeScanner(it1, it) } }!!
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                isQRcodeValidCheck(it)
                //openAR(it)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(
                    activity, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Camera permission granted", Toast.LENGTH_LONG).show()
                startScanning()
            } else {
                Toast.makeText(activity, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
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

    private fun checkPermissions() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.CAMERA),
                    0
                )
            }
        }
    }

    private fun openAR(result: Result) {
        val bundle = bundleOf("arItemId" to result.text)

        Log.d("artest", "qrscanF QR id: $result")

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ArModeFragment>(R.id.fragmentContainer, args = bundle)
            addToBackStack(null)
        }
    }

    private fun isQRcodeValidCheck(result: Result) {

        val inTest = result.toString()

        if (inTest.length == 24) {
            if (isLettersOrNumbers(inTest)) {
                fetchQRItemData(inTest)
                //openAR(result)
            } else {
                Toast.makeText(activity, "Not a valid QR code", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(activity, "Not a valid QR code", Toast.LENGTH_LONG).show()
        }
    }

    private fun isLettersOrNumbers(string: String): Boolean {
        for (c in string) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

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

    private fun mapActionsDialog(
        arItemId: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?
    ) {

        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
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

    private fun locationMapDialog(latitude: Double?, longitude: Double?) {

        if (latitude != null && longitude != null) {

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
            val startPoint = GeoPoint(latitude, longitude)
            mapController.setCenter(startPoint)


/*        marker = Marker(map)
        marker.icon = AppCompatResources.getDrawable(this.requireContext(), R.drawable.ic_pin)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.textLabelFontSize = 20*/

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