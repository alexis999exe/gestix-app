package com.example.reloj.data.network

import com.example.reloj.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface GestixApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("tickets")
    suspend fun getTickets(@Header("Authorization") token: String): Response<DataWrapper<List<Ticket>>>

    @PUT("tickets/{id}")
    suspend fun updateTicket(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<DataWrapper<Ticket>>
}
