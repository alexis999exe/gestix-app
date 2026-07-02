package com.example.gesticks.data.network

import com.example.gesticks.data.model.LoginRequest
import com.example.gesticks.data.model.LoginResponse
import com.example.gesticks.data.model.Ticket
import retrofit2.Response
import retrofit2.http.*

interface GestixApiService {
    
    // Autenticación
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Tickets
    @GET("tickets")
    suspend fun getTickets(
        @Header("Authorization") token: String
    ): Response<List<Ticket>>

    @POST("tickets")
    suspend fun createTicket(
        @Header("Authorization") token: String,
        @Body ticket: Ticket
    ): Response<Ticket>

    @PATCH("tickets/{id}")
    suspend fun updateTicketPriority(
        @Header("Authorization") token: String,
        @Path("id") id: String, 
        @Body body: Map<String, String>
    ): Response<Ticket>
}
