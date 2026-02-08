package com.gaizkafrost.mentxuapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.EstadoParada
import com.mapbox.geojson.Point

/**
 * Entidad de Room para almacenar paradas localmente (offline cache)
 */
@Entity(tableName = "paradas")
data class ParadaEntity(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val nombreCorto: String?,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String?,
    val tipoJuego: String?,
    val orden: Int,
    val imagenUrl: String?,
    val estado: String = "bloqueada", // "bloqueada", "activa", "completada"
    val ultimaActualizacion: Long = System.currentTimeMillis()
) {
    /**
     * Convierte ParadaEntity (Room) a Parada (domain model usado en la app)
     */
    fun toParada(): Parada {
        return Parada(
            id = id,
            nombre = nombreCorto ?: nombre,
            ubicacion = Point.fromLngLat(longitud, latitud),
            estado = when (estado) {
                "activa" -> EstadoParada.ACTIVA
                "completada" -> EstadoParada.COMPLETADA
                else -> EstadoParada.BLOQUEADA
            }
        )
    }
}
