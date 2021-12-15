package com.virtualmapdevs.area_explorer.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message") val message: String
)