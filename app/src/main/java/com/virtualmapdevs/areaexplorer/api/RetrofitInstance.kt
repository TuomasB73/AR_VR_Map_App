package com.virtualmapdevs.areaexplorer.api

import com.virtualmapdevs.areaexplorer.utils.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ArVrApi by lazy {
        retrofit.create(ArVrApi::class.java)
    }
}