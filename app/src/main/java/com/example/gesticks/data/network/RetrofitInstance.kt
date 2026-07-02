package com.example.gesticks.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Nota: Deberás reemplazar esto con la URL de tu API intermedia (Node.js, etc.)
    // que se conecta a MongoDB, ya que no se recomienda conectar directo desde Android.
    private const val BASE_URL = "https://gestix-backend.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS) // Aumentado a 120s para servidores lentos
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    val api: GestixApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GestixApiService::class.java)
    }
}
