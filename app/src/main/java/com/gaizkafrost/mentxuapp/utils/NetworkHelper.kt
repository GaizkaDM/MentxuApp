package com.gaizkafrost.mentxuapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Helper para verificar la conectividad de red y el tipo de conexión actual.
 * Maneja las diferencias entre versiones de Android (pre y post Marshmallow).
 *
 * @author Diego, Gaizka, Xiker
 */
object NetworkHelper {
    
    /**
     * Comprueba si el dispositivo tiene una conexión a internet activa y validada.
     * @param context Contexto de la aplicación.
     * @return `true` si hay conexión, `false` en caso contrario.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        // ... implementation
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Determina el tipo de red al que está conectado el dispositivo.
     * @param context Contexto de la aplicación.
     * @return Enum [NetworkType] indicando el tipo (WIFI, CELULAR, etc.).
     */
    fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                else -> if (networkInfo?.isConnected == true) NetworkType.OTHER else NetworkType.NONE
            }
        }
    }
}

/** Enum que define los tipos de conexión posibles. */
enum class NetworkType {
    NONE, WIFI, CELLULAR, ETHERNET, OTHER
}
