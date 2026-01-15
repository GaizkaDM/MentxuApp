package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== REQUEST ====================

data class CompletarParadaRequest(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    @SerializedName("parada_id")
    val paradaId: Int,
    val puntuacion: Int = 0,
    @SerializedName("tiempo_empleado")
    val tiempoEmpleado: Int? = null,
    val intentos: Int? = null
)

data class ActualizarProgresoRequest(
    val puntuacion: Int? = null,
    @SerializedName("tiempo_empleado")
    val tiempoEmpleado: Int? = null,
    val intentos: Int? = null
)

// ==================== RESPONSE ====================

data class CompletarParadaResponse(
    val mensaje: String,
    val progreso: ProgresoItemResponse,
    @SerializedName("siguiente_parada_id")
    val siguienteParadaId: Int?
)

data class ProgresoUsuarioResponse(
    @SerializedName("usuario_id")
    val usuarioId: Int,
    @SerializedName("nombre_completo")
    val nombreCompleto: String,
    val progreso: List<ProgresoItemResponse>
)

data class ProgresoItemResponse(
    val id: Int,
    @SerializedName("usuario_id")
    val usuarioId: Int,
    @SerializedName("parada_id")
    val paradaId: Int,
    val estado: String, // "bloqueada", "activa", "completada"
    @SerializedName("fecha_inicio")
    val fechaInicio: String?,
    @SerializedName("fecha_completado")
    val fechaCompletado: String?,
    val puntuacion: Int?,
    @SerializedName("tiempo_empleado")
    val tiempoEmpleado: Int?,
    val intentos: Int?
)

data class EstadisticasGeneralesResponse(
    @SerializedName("total_usuarios")
    val totalUsuarios: Int,
    @SerializedName("total_paradas")
    val totalParadas: Int,
    @SerializedName("total_completados")
    val totalCompletados: Int,
    @SerializedName("total_activos")
    val totalActivos: Int,
    @SerializedName("parada_mas_popular")
    val paradaMasPopular: String?,
    @SerializedName("usuarios_completaron_todo")
    val usuariosCompletaronTodo: Int
)
