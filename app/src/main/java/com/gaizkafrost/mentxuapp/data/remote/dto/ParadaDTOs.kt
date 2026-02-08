package com.gaizkafrost.mentxuapp.data.remote.dto

import com.gaizkafrost.mentxuapp.data.local.entity.ParadaEntity
import com.mapbox.geojson.Point
import com.google.gson.annotations.SerializedName

// ==================== RESPONSE ====================

data class ParadaResponse(
    val id: Int,
    val nombre: String,
    @SerializedName("nombre_corto")
    val nombreCorto: String?,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String?,
    @SerializedName("tipo_juego")
    val tipoJuego: String?,
    val orden: Int,
    @SerializedName("imagen_url")
    val imagenUrl: String?
) {
    /**
     * Convierte ParadaResponse (API) a ParadaEntity (Room Database)
     */
    fun toEntity(estado: String = "bloqueada"): ParadaEntity {
        return ParadaEntity(
            id = id,
            nombre = nombre,
            nombreCorto = nombreCorto,
            latitud = latitud,
            longitud = longitud,
            descripcion = descripcion,
            tipoJuego = tipoJuego,
            orden = orden,
            imagenUrl = imagenUrl,
            estado = estado,
            ultimaActualizacion = System.currentTimeMillis()
        )
    }
    
    /**
     * Helper para obtener Point para Mapbox Maps
     */
    fun getLocation(): Point = Point.fromLngLat(longitud, latitud)
}

data class EstadisticasParadaResponse(
    val parada: ParadaResponse,
    @SerializedName("total_completados")
    val totalCompletados: Int,
    @SerializedName("total_activos")
    val totalActivos: Int,
    @SerializedName("tiempo_promedio_segundos")
    val tiempoPromedioSegundos: Int
)
