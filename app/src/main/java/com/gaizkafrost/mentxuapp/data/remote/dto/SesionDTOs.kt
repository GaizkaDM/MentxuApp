package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para el sistema de sesiones de usuario
 */

// ==================== REGISTRAR SESIÓN ====================

/**
 * Request para registrar una nueva sesión de usuario
 */
data class RegistrarSesionRequest(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("tipo_dispositivo")
    val tipoDispositivo: String = "android",
    
    @SerializedName("device_info")
    val deviceInfo: String? = null
)

/**
 * Respuesta al registrar una sesión
 */
data class RegistrarSesionResponse(
    val mensaje: String,
    val sesion: SesionInfo
)

/**
 * Información de una sesión
 */
data class SesionInfo(
    val id: Int,
    
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("fecha_inicio")
    val fechaInicio: String,
    
    @SerializedName("fecha_fin")
    val fechaFin: String?,
    
    @SerializedName("tipo_dispositivo")
    val tipoDispositivo: String,
    
    @SerializedName("device_info")
    val deviceInfo: String?,
    
    val estado: String,
    
    @SerializedName("duracion_segundos")
    val duracionSegundos: Int,
    
    @SerializedName("duracion_formateada")
    val duracionFormateada: String?
)

// ==================== CERRAR SESIÓN ====================

/**
 * Respuesta al cerrar una sesión
 */
data class CerrarSesionResponse(
    val mensaje: String,
    val sesion: SesionInfo
)
