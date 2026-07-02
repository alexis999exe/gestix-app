package com.example.gesticks.data.repository

import com.example.gesticks.data.model.LoginRequest
import com.example.gesticks.data.model.LoginResponse
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.data.network.RetrofitInstance
import retrofit2.Response

class TicketRepository {
    private val api = RetrofitInstance.api

    suspend fun login(correo: String, contrasena: String): Response<LoginResponse> {
        return api.login(LoginRequest(correo, contrasena))
    }

    suspend fun getTickets(token: String): Response<List<Ticket>> {
        return api.getTickets("Bearer $token")
    }

    suspend fun createTicket(token: String, ticket: Ticket): Response<Ticket> {
        return api.createTicket("Bearer $token", ticket)
    }

    suspend fun updatePriority(token: String, id: String, prioridad: String): Response<Ticket> {
        return api.updateTicketPriority("Bearer $token", id, mapOf("prioridad" to prioridad))
    }
}
