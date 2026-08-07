package com.example.reloj.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    @SerializedName("nombre") val name: String,
    @SerializedName("correo") val email: String
)

data class Ticket(
    @SerializedName("id") val id: Int,
    @SerializedName("titulo") val title: String,
    @SerializedName("descripcion") val description: String,
    @SerializedName("prioridad") val priority: String, // baja|media|alta|critica
    @SerializedName("estado") val status: String = "abierto", // abierto|pendiente|resuelto
    @SerializedName("fecha_creacion") val date: String,
    @SerializedName("usuario_autor_id") val authorId: Int
)

data class LoginRequest(
    @SerializedName("correo") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("access_token") val accessToken: String?,
    val user: User?
)

data class DataWrapper<T>(
    val data: T
)

data class UpdateStatusRequest(
    @SerializedName("estado") val status: String
)
