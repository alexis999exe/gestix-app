package com.example.gesticks.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.data.network.SessionManager
import com.example.gesticks.data.repository.TicketRepository
import com.example.gesticks.wear.WearSyncManager
import kotlinx.coroutines.launch

class TicketsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TicketRepository()
    private val sessionManager = SessionManager(application)
    private val wearSyncManager = WearSyncManager(application)

    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _activeCount = MutableLiveData(0)
    val activeCount: LiveData<Int> = _activeCount

    private val _resolvedCount = MutableLiveData(0)
    val resolvedCount: LiveData<Int> = _resolvedCount

    init {
        fetchTickets()
    }

    fun fetchTickets() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            // Manejar error de sesión
            return
        }

        val userId = sessionManager.fetchUserId()
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = repository.getTickets(token)
                if (response.isSuccessful) {
                    val allTickets = response.body() ?: emptyList()
                    // Filtramos por usuario_autor_id (tu ID de usuario)
                    val userTickets = allTickets.filter { it.authorId == userId }
                    _tickets.value = userTickets
                    
                    // Sincronizar con el reloj
                    wearSyncManager.syncTickets(userTickets)
                    
                    _activeCount.value = userTickets.count { it.status.lowercase() != "cerrado" && it.status.lowercase() != "resuelto" }
                    _resolvedCount.value = userTickets.count { it.status.lowercase() == "cerrado" || it.status.lowercase() == "resuelto" }
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
