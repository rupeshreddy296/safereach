package com.example.safereach.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 connects the Android Emulator to your computer's localhost.
    // NOTE: Do not include "htdocs" in the URL. XAMPP serves files directly from that folder.
    private const val BASE_URL = "http://192.168.1.21/safereach/"

    private var authToken: String? = null

    /**
     * Sets the active bearer token for API calls
     */
    fun setToken(token: String?) {
        authToken = token
    }

    /**
     * Returns the active bearer token
     */
    fun getToken(): String? {
        return authToken
    }

    // Interceptor to inject bearer token in request headers
    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            authToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
            
            val request = requestBuilder
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
                .build()
                
            return chain.proceed(request)
        }
    }

    // Logging interceptor for debugging network packets
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
