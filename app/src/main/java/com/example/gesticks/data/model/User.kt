package com.example.gesticks.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val objectId: String? = null,
    val id: Int,
    @SerializedName("nombre")
    val name: String,
    @SerializedName("correo")
    val email: String,
    @SerializedName("telefono")
    val phone: String? = null,
    @SerializedName("contrasena")
    val password: String,
    @SerializedName("estatus")
    val status: Int,
    @SerializedName("departamento_id")
    val departmentId: Int,
    @SerializedName("permisos")
    val permissions: List<Int>
)

data class LoginResponse(
    val token: String?,
    val user: User?
)
