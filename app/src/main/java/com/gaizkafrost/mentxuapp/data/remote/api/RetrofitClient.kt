package com.gaizkafrost.mentxuapp.data.remote.api

import com.gaizkafrost.mentxuapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para las llamadas HTTP
 * Configurado con logging, timeouts y la base URL del backend
 */
object RetrofitClient {
    
    // La URL se toma de BuildConfig que se configuró en build.gradle.kts
    private const val BASE_URL = BuildConfig.API_BASE_URL
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // Logging completo en debug
        } else {
            HttpLoggingInterceptor.Level.NONE // Sin logging en release
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: MentxuApi by lazy {
        retrofit.create(MentxuApi::class.java)
    }
    
    /**
     * Para testing o cambio dinámico de URL
     */
    fun createApi(baseUrl: String): MentxuApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MentxuApi::class.java)
    }
}
