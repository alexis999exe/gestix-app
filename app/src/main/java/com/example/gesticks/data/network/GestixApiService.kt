package com.example.gesticks.data.network

import com.example.gesticks.data.model.DataWrapper
import com.example.gesticks.data.model.LoginRequest
import com.example.gesticks.data.model.LoginResponse
import com.example.gesticks.data.model.Ticket
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface GestixApiService {
    
    // Autenticación
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RequestBody): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<DataWrapper<com.example.gesticks.data.model.User>>

    // Tickets
    @GET("tickets")
    suspend fun getTickets(
        @Header("Authorization") token: String
    ): Response<DataWrapper<List<Ticket>>>

    @GET("tickets/{id}")
    suspend fun getTicket(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DataWrapper<Ticket>>

    @Multipart
    @POST("tickets")
    suspend fun createTicket(
        @Header("Authorization") token: String,
        @Part("titulo") title: RequestBody,
        @Part("descripcion") description: RequestBody,
        @Part("prioridad") priority: RequestBody,
        @Part("categoria_id") categoryId: RequestBody,
        @Part archivo: MultipartBody.Part? = null
    ): Response<DataWrapper<Ticket>>

    @PUT("tickets/{id}")
    suspend fun updateTicket(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body ticket: Ticket
    ): Response<DataWrapper<Ticket>>

    @POST("tickets/{id}/resolve")
    suspend fun resolveTicket(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DataWrapper<Ticket>>

    @DELETE("tickets/{id}")
    suspend fun deleteTicket(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>
}
