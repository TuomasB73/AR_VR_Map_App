package com.virtualmapdevs.area_explorer.api

import com.virtualmapdevs.area_explorer.utils.Constants.Companion.BASE_URL
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