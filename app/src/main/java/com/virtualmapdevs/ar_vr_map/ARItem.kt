package com.virtualmapdevs.ar_vr_map

import com.google.gson.annotations.SerializedName

data class ARItem (

    @SerializedName("_id") val _id : String,
    @SerializedName("userId") val userId : String,
    @SerializedName("type") val type : String,
    @SerializedName("imageReference") val imageReference : String,
    @SerializedName("name") val name : String,
    @SerializedName("description") val description : String,
    @SerializedName("latitude") val latitude : Double,
    @SerializedName("longitude") val longitude : Double,
    @SerializedName("category") val category : String,
    @SerializedName("QRCode") val qRCode : String,
    @SerializedName("__v") val __v : Int
)
