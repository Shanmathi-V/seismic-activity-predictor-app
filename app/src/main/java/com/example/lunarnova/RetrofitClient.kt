package com.example.lunarnova
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://nasa-api-seismic-waves.onrender.com/"

    // Create OkHttpClient with custom timeouts
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)  // Connection timeout
            .writeTimeout(120, TimeUnit.SECONDS)    // Write timeout
            .readTimeout(120, TimeUnit.SECONDS)     // Read timeout
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Set the OkHttpClient with timeout settings
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}