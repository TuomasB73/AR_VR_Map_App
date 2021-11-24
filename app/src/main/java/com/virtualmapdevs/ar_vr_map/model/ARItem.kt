package com.virtualmapdevs.ar_vr_map.model

import com.google.gson.annotations.SerializedName

data class ARItem(
    @SerializedName("_id") val _id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("type") val type: String,
    @SerializedName("imageReference") val imageReference: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("category") val category: String,
    @SerializedName("__v") val __v: Int
)

data class Poi(
    val id: String,
    val name: String,
    val description: String,
    val x: Float,
    val y: Float,
    val z: Float
)