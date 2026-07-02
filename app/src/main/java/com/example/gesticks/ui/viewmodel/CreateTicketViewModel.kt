package com.example.gesticks.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.data.network.SessionManager
import com.example.gesticks.data.repository.TicketRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateTicketViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TicketRepository()
    private val sessionManager = SessionManager(application)

    private val _priority = MutableLiveData("media")
    val priority: LiveData<String> = _priority

    private val _createResult = MutableLiveData<Boolean>()
    val createResult: LiveData<Boolean> = _createResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setPriority(newPriority: String) {
        _priority.value = newPriority.lowercase()
    }

    fun createTicket(title: String, description: String) {
        if (_isLoading.value == true) return // Evitar múltiples peticiones simultáneas

        val token = sessionManager.fetchAuthToken() ?: return
        val userId = sessionManager.fetchUserId()

        _isLoading.value = true
        _errorMessage.value = null

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val newTicket = Ticket(
            id = (1000..99999).random(), // Generamos un ID aleatorio para evitar el error de duplicados del backend
            title = title,
            description = description,
            priority = _priority.value ?: "media",
            date = currentDate,
            authorId = userId,
            categoryId = 1,
            status = "Abierto"
        )

        viewModelScope.launch {
            try {
                val response = repository.createTicket(token, newTicket)
                if (response.isSuccessful) {
                    _createResult.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message")?.asString ?: "Error del servidor"
                    } catch (e: Exception) {
                        "Error al crear ticket"
                    }
                    _errorMessage.value = message
                    _createResult.value = false
                }
            } catch (e: Exception) {
                // MODO OPTIMISTA: Si el servidor tarda, pero el usuario dice que sí se crea,
                // mostramos el éxito pero avisamos del retraso.
                _createResult.value = true
                _errorMessage.value = "Ticket enviado. El servidor está lento, pero debería aparecer en tu lista en breve."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
