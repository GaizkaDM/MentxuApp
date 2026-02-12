package com.gaizkafrost.mentxuapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.EstadoParada
import com.mapbox.geojson.Point

/**
 * Entidad de Base de Datos (Room) que representa una parada almacenada localmente.
 * Permite que la aplicación funcione sin conexión a internet (offline-first).
 *
 * @property id Identificador único de la parada (coincide con el remoto).
 * @property nombre Nombre completo de la parada.
 * @property nombreCorto Nombre abreviado para mostrar en listas o marcadores.
 * @property latitud Coordenada de latitud.
 * @property longitud Coordenada de longitud.
 * @property descripcion Descripción detallada del lugar o actividad.
 * @property tipoJuego Identificador del tipo de minijuego asociado (puzzle, quiz, etc.).
 * @property orden Orden de la parada en la ruta.
 * @property imagenUrl URL de la imagen representativa (puede ser local o remota).
 * @property estado Estado de progreso actual ("bloqueada", "activa", "completada").
 * @property ultimaActualizacion Timestamp de la última vez que se sincronizó este registro.
 * @author Diego, Gaizka, Xiker
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
     * Convierte esta entidad de base de datos al modelo de dominio [Parada].
     * Realiza la transformación de estados de String a Enum [EstadoParada].
     *
     * @return Objeto [Parada] listo para ser usado por la UI o lógica de negocio.
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
