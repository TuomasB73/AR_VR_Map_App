package com.virtualmapdevs.areaexplorer.repository

import android.util.Log
import com.virtualmapdevs.areaexplorer.model.ARItem
import com.virtualmapdevs.areaexplorer.api.RetrofitInstance
import com.virtualmapdevs.areaexplorer.model.Message
import com.virtualmapdevs.areaexplorer.model.User
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

    suspend fun postUserScannedItem(token: String, id: String): Response<Message> {
        return RetrofitInstance.api.postUserScannedItem(token, id)
    }

    suspend fun deleteUserScannedItem(token: String, id: String): Response<Message> {
        return RetrofitInstance.api.deleteUserScannedItem(token, id)
    }

    suspend fun getUserScannedItems(token: String): Response<List<ARItem>> {
        return RetrofitInstance.api.getUserScannedItems(token)
    }
}