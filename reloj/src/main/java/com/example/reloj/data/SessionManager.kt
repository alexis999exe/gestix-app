package com.example.reloj.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gestix_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun clearSession() {
        prefs.edit().remove("auth_token").apply()
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    fun saveTicketsCache(json: String) {
        prefs.edit().putString("tickets_cache", json).apply()
    }

    fun getTicketsCache(): String? {
        return prefs.getString("tickets_cache", null)
    }
}
