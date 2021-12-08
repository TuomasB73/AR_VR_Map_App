package com.virtualmapdevs.ar_vr_map.model

import android.view.MenuItem
import com.google.gson.annotations.SerializedName
import com.virtualmapdevs.ar_vr_map.utils.RotatingNode

data class ARItem(
    @SerializedName("pois") val pois: List<Poi>,
    @SerializedName("_id") val _id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("type") val type: String,
    @SerializedName("objectReference") val objectReference: String,
    @SerializedName("logoImageReference") val logoImageReference: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("category") val category: String,
)

data class Poi(

    @SerializedName("poiId") val poiId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("mapCoordinates") val mapCoordinates: MapCoordinates,
    @SerializedName("poiImage") val poiImage: String
)

data class MapCoordinates(

    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("z") val z: Float
)

data class ReducedPoi(
    val name: String,
    val distance: Int,
    val x: Float,
    val y: Float,
    val z: Float
)

data class AddedPointOfInterest(
    val poi: Poi,
    val menuItem: MenuItem,
    val node: RotatingNode
)