package com.virtualmapdevs.ar_vr_map.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("z") val z: Float,
    @SerializedName("poiImage") val poiImage: String
)

data class ReducedPoi(
    val name: String,
    val distance: Int,
    val x: Float,
    val y: Float,
    val z: Float
)