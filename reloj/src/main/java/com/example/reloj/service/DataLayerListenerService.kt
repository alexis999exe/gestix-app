package com.example.reloj.service

import android.content.Intent
import android.util.Log
import com.example.reloj.data.SessionManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataLayerListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("GestixWear", "onDataChanged: $dataEvents")
        val sessionManager = SessionManager(this)
        
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/tickets") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                
                // Guardar token si viene
                dataMap.getString("auth_token")?.let { token ->
                    sessionManager.saveAuthToken(token)
                }

                val userId = dataMap.getInt("user_id", -1)
                if (userId != -1) {
                    sessionManager.saveUserId(userId)
                }
                
                // Guardar cache de tickets
                dataMap.getString("tickets_json")?.let { json ->
                    sessionManager.saveTicketsCache(json)
                }
                
                // Notificar a la UI que hay nuevos datos (opcional si la UI ya escucha)
                // Aquí solo nos aseguramos de que el persistido esté actualizado
            }
        }
    }
}
