package com.example.gesticks.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesticks.data.network.SessionManager
import com.example.gesticks.data.repository.TicketRepository
import com.example.gesticks.wear.WearSyncManager
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TicketRepository()
    private val sessionManager = SessionManager(application)
    private val wearSyncManager = WearSyncManager(application)

    private val _countOpen = MutableLiveData(0)
    val countOpen: LiveData<Int> = _countOpen

    private val _countProcess = MutableLiveData(0)
    val countProcess: LiveData<Int> = _countProcess

    private val _countResolved = MutableLiveData(0)
    val countResolved: LiveData<Int> = _countResolved

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    fun loadData() {
        _userName.value = sessionManager.fetchUserName().split(" ")[0] // Solo el primer nombre

        val token = sessionManager.fetchAuthToken() ?: return
        val userId = sessionManager.fetchUserId()

        viewModelScope.launch {
            try {
                val response = repository.getTickets(token)
                if (response.isSuccessful) {
                    val allTickets = response.body() ?: emptyList()
                    val userTickets = allTickets.filter { it.authorId == userId }
                    
                    // Sincronizar con el reloj
                    wearSyncManager.syncTickets(userTickets)
                    
                    _countOpen.value = userTickets.count { it.status.lowercase() == "abierto" }
                    _countProcess.value = userTickets.count { it.status.lowercase() == "en proceso" }
                    _countResolved.value = userTickets.count { it.status.lowercase() == "cerrado" || it.status.lowercase() == "resuelto" }
                }
            } catch (e: Exception) {
                // Error
            }
        }
    }
}
