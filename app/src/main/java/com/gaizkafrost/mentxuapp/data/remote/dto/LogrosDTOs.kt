package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para el sistema de logros
 */

// ==================== VERIFICAR LOGROS ====================

/**
 * Respuesta de verificación de logros
 */
data class VerificarLogrosResponse(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("nuevos_logros")
    val nuevosLogros: List<LogroDesbloqueado>,
    
    @SerializedName("total_verificados")
    val totalVerificados: Int
)

/**
 * Logro recién desbloqueado
 */
data class LogroDesbloqueado(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val tipo: String,
    val puntos: Int,
    val icono: String?,
    val color: String?
)

// ==================== LOGROS USUARIO ====================

/**
 * Respuesta con todos los logros del usuario
 */
data class LogrosUsuarioResponse(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("total_logros")
    val totalLogros: Int,
    
    @SerializedName("logros_completados")
    val logrosCompletados: Int,
    
    @SerializedName("puntos_totales")
    val puntosTotales: Int,
    
    @SerializedName("logros")
    val logros: List<LogroUsuario>
)

/**
 * Logro individual con estado del usuario
 */
data class LogroUsuario(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val tipo: String,
    val dificultad: String?,
    val puntos: Int,
    val icono: String?,
    val color: String?,
    val secreto: Boolean = false,
    
    @SerializedName("desbloqueado")
    val desbloqueado: Boolean = false,
    
    @SerializedName("fecha_desbloqueo")
    val fechaDesbloqueo: String? = null,
    
    val progreso: Int = 0
)

// ==================== REGISTRAR INTENTO ====================

/**
 * Request para registrar un intento en una actividad
 */
data class RegistrarIntentoRequest(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    
    @SerializedName("parada_id")
    val paradaId: Int,
    
    @SerializedName("tipo_actividad")
    val tipoActividad: String,
    
    val puntuacion: Int,
    
    @SerializedName("tiempo_segundos")
    val tiempoSegundos: Int,
    
    val resultado: String = "exito",
    
    val errores: Int = 0,
    
    @SerializedName("pistas_usadas")
    val pistasUsadas: Int = 0
)

/**
 * Respuesta del registro de intento
 */
data class RegistrarIntentoResponse(
    val id: Int,
    
    @SerializedName("numero_intento")
    val numeroIntento: Int,
    
    val mensaje: String?
)
