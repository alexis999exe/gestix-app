package com.example.gesticks.data.model

import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("_id")
    val objectId: String? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("titulo")
    val title: String,
    @SerializedName("descripcion")
    val description: String,
    @SerializedName("prioridad")
    val priority: String,
    @SerializedName("fecha_creacion")
    val date: String,
    @SerializedName("fecha_asignacion")
    val assignedDate: String? = null,
    @SerializedName("fecha_resolucion")
    val resolutionDate: String? = null,
    @SerializedName("usuario_autor_id")
    val authorId: Int,
    @SerializedName("categoria_id")
    val categoryId: Int,
    @SerializedName("comentarios")
    val comments: List<Int> = emptyList(),
    // Mantenemos status para la UI, aunque lo calcularemos o mapearemos
    val status: String = "Abierto"
)
