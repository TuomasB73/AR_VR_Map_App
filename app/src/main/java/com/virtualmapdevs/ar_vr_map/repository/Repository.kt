package com.virtualmapdevs.ar_vr_map.repository

import android.util.Log
import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.api.RetrofitInstance
import com.virtualmapdevs.ar_vr_map.model.Message
import com.virtualmapdevs.ar_vr_map.model.User
import retrofit2.Response

class Repository {

    suspend fun getMessage(): Response<Message> {
        Log.d("artest", "in repo")
        return RetrofitInstance.api.getMessage()
    }

    suspend fun postUser(user: User): Response<Message> {
        return RetrofitInstance.api.postUser(user)
    }

    suspend fun registerUser(name: String, passWord: String): Response<Message> {
        return RetrofitInstance.api.registerUser(name, passWord)
    }

    suspend fun loginUser(name: String, passWord: String): Response<Message> {
        return RetrofitInstance.api.loginUser(name, passWord)
    }

    suspend fun getSecureData(token: String): Response<Message> {
        return RetrofitInstance.api.getSecureData(token)
    }


    suspend fun getArItemById(
        token: String,
        id: String,
    ): Response<ARItem> {
        return RetrofitInstance.api.getArItemById(token, id)
    }
}