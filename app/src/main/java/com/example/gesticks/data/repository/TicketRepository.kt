package com.example.gesticks.data.repository

import com.example.gesticks.data.model.DataWrapper
import com.example.gesticks.data.model.LoginRequest
import com.example.gesticks.data.model.LoginResponse
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.data.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class TicketRepository {
    private val api = RetrofitInstance.api

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return api.login(LoginRequest(email, password))
    }

    suspend fun getTickets(token: String): Response<DataWrapper<List<Ticket>>> {
        return api.getTickets("Bearer $token")
    }

    suspend fun createTicket(
        token: String,
        titulo: String,
        descripcion: String,
        prioridad: String,
        categoriaId: Int,
        archivo: File? = null
    ): Response<DataWrapper<Ticket>> {
        val titleRB = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val descRB = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val priorityRB = prioridad.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryIdRB = categoriaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val bodyArchivo = archivo?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("archivo", it.name, requestFile)
        }

        return api.createTicket(
            "Bearer $token",
            titleRB,
            descRB,
            priorityRB,
            categoryIdRB,
            bodyArchivo
        )
    }

    suspend fun resolveTicket(token: String, id: Int): Response<DataWrapper<Ticket>> {
        return api.resolveTicket("Bearer $token", id)
    }

    suspend fun deleteTicket(token: String, id: Int): Response<Void> {
        return api.deleteTicket("Bearer $token", id)
    }
}
