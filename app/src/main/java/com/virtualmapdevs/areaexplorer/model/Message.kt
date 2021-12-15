package com.virtualmapdevs.areaexplorer.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message") val message: String
)