package com.example.gesticks.data.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gestix_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun fetchUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun fetchUserName(): String {
        return prefs.getString("user_name", "Usuario") ?: "Usuario"
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun fetchUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
