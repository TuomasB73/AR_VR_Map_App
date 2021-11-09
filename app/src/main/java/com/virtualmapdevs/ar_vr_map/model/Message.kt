package com.virtualmapdevs.ar_vr_map.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message") val message: String
)