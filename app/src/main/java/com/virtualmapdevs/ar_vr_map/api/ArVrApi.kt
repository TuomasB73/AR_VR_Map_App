package com.virtualmapdevs.ar_vr_map.api

import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.model.Message
import com.virtualmapdevs.ar_vr_map.model.User
import retrofit2.Response
import retrofit2.http.*

interface ArVrApi {
    @GET("auth/register")
    suspend fun getMessage(): Response<Message>

    @POST("auth/register")
    suspend fun postUser(
        @Body user: User
    ): Response<Message>

    @FormUrlEncoded
    @POST("auth/register")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<Message>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<Message>

    @GET("aritem/")
    suspend fun getSecureData(@Header("Authorization") token: String): Response<Message>

    @GET("aritem/{id}")
    suspend fun getArItemById(
        @Header("Authorization") token: String,
        @Path(value = "id", encoded = true) id: String,
    ): Response<ARItem>
}