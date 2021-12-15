package com.virtualmapdevs.areaexplorer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.virtualmapdevs.areaexplorer.fragments.ArModeFragment
import com.virtualmapdevs.areaexplorer.utils.Constants.Companion.PERMISSIONS_REQUEST_LOCATION
import org.osmdroid.util.GeoPoint

class LocationManager(val context: Context, val fragment: ArModeFragment) {

    lateinit var fusedLocationClient:
            FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    var locationRequest: LocationRequest? = null
    var userLocation : GeoPoint? = null

    fun initLocationClientRequestAndCallback() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


        locationRequest = LocationRequest
            .create()
            .setInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                }
            }
        }
    }

    fun checkSelfPermissions() {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted so requesting of location updates is not possible
            // Prompt user to grant them then listen to callback whether they were granted or not
            Log.d("location", "failed, didn't pass")
            android.app.AlertDialog.Builder(context)
                .setTitle("Location Permission required")
                .setMessage("This application needs the Location permission, please accept to use location functionality")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    //Prompt the user once explanation has been shown
                    // FIXME: deprecation
                    fragment.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_LOCATION
                    )
                }
                .create()
                .show()

        } else {
            // Permissions are granted so start requesting location updates
            Log.d("location", "Permissions are granted")

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d(
                            "location", "Got last known location: ${
                                GeoPoint(
                                    location.latitude,
                                    location.longitude
                                )
                            }"
                        )
                    }
                }
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!,
                locationCallback, Looper.getMainLooper()
            )

        }
    }

}