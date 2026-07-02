package com.example.gesticks.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesticks.data.network.SessionManager
import com.example.gesticks.data.repository.TicketRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TicketRepository()
    private val sessionManager = SessionManager(application)

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _totalTickets = MutableLiveData(0)
    val totalTickets: LiveData<Int> = _totalTickets

    private val _resolvedTickets = MutableLiveData(0)
    val resolvedTickets: LiveData<Int> = _resolvedTickets

    private val _activeTickets = MutableLiveData(0)
    val activeTickets: LiveData<Int> = _activeTickets

    fun loadUserData() {
        _userName.value = sessionManager.fetchUserName()
        _userEmail.value = sessionManager.fetchUserEmail()

        val token = sessionManager.fetchAuthToken() ?: return
        val userId = sessionManager.fetchUserId()

        viewModelScope.launch {
            try {
                val response = repository.getTickets(token)
                if (response.isSuccessful) {
                    val allTickets = response.body() ?: emptyList()
                    val userTickets = allTickets.filter { it.authorId == userId }
                    
                    _totalTickets.value = userTickets.size
                    _resolvedTickets.value = userTickets.count { it.status.lowercase() == "cerrado" || it.status.lowercase() == "resuelto" }
                    _activeTickets.value = userTickets.count { it.status.lowercase() != "cerrado" && it.status.lowercase() != "resuelto" }
                }
            } catch (e: Exception) {
                // Error
            }
        }
    }
}
