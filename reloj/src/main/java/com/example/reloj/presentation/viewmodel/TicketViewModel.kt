package com.example.reloj.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reloj.data.SessionManager
import com.example.reloj.data.model.*
import com.example.reloj.data.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class TicketViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val gson = Gson()
    
    var tickets by mutableStateOf<List<Ticket>>(loadFromCache())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(sessionManager.isLoggedIn())

    fun login(email: String, pass: String) {
        // ... (can keep it or remove it, I'll keep it as fallback but mainly rely on sync)
    }

    fun setToken(token: String) {
        if (sessionManager.getAuthToken() != token) {
            sessionManager.saveAuthToken(token)
            isLoggedIn = true
            fetchTickets()
        }
    }

    fun setUserId(userId: Int) {
        sessionManager.saveUserId(userId)
    }

    fun updateTicketsFromJson(json: String) {
        try {
            val type = object : TypeToken<List<Ticket>>() {}.type
            val newTickets: List<Ticket> = gson.fromJson(json, type)
            tickets = newTickets.sortedByDescending { 
                when(it.priority.lowercase()) {
                    "critica" -> 3
                    "alta" -> 2
                    "media" -> 1
                    else -> 0
                }
            }
            sessionManager.saveTicketsCache(json)
            error = null
        } catch (e: Exception) {
            // Error parsing
        }
    }

    fun fetchTickets() {
        val token = sessionManager.getAuthToken() ?: return
        val userId = sessionManager.getUserId()
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitInstance.api.getTickets("Bearer $token")
                if (response.isSuccessful) {
                    val allTickets = response.body()?.data ?: emptyList()
                    val filteredTickets = if (userId != -1) {
                        allTickets.filter { it.authorId == userId }
                    } else {
                        allTickets
                    }
                    
                    val sortedTickets = filteredTickets.sortedByDescending { 
                        when(it.priority.lowercase()) {
                            "critica" -> 3
                            "alta" -> 2
                            "media" -> 1
                            else -> 0
                        }
                    }
                    tickets = sortedTickets
                    sessionManager.saveTicketsCache(gson.toJson(sortedTickets))
                    error = null
                }
            } catch (e: Exception) {
                error = "Sin conexión"
                if (tickets.isEmpty()) {
                    tickets = loadFromCache()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun updateStatus(ticketId: Int, newStatus: String) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateTicket(
                    "Bearer $token",
                    ticketId,
                    UpdateStatusRequest(newStatus)
                )
                if (response.isSuccessful) {
                    fetchTickets() // Refresh list
                }
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        sessionManager.saveTicketsCache("")
        isLoggedIn = false
    }

    private fun loadFromCache(): List<Ticket> {
        val json = sessionManager.getTicketsCache() ?: return emptyList()
        if (json.isEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<Ticket>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
