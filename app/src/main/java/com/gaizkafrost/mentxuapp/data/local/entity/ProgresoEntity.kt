package com.gaizkafrost.mentxuapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Base de Datos (Room) que rastrea el progreso detallado del usuario en una parada específica.
 *
 * Mantiene una relación de clave foránea con [ParadaEntity] para asegurar la integridad referencial.
 * Si se borra una parada, se borra su progreso asociado (Cascade).
 *
 * @property id Identificador único del registro de progreso (autogenerado).
 * @property usuarioId ID del usuario al que pertenece este progreso.
 * @property paradaId ID de la parada asociada.
 * @property estado Estado actual del progreso ("bloqueada", "activa", "completada").
 * @property fechaInicio Timestamp de cuándo se inició la actividad.
 * @property fechaCompletado Timestamp de cuándo se completó la actividad (null si no completada).
 * @property puntuacion Puntuación obtenida en el minijuego de la parada.
 * @property tiempoEmpleado Tiempo total en segundos que tomó completar la actividad.
 * @property intentos Número de intentos realizados.
 * @property sincronizado Indica si este registro ya ha sido enviado exitosamente al servidor remoto.
 * @author Diego, Gaizka, Xiker
 */
@Entity(
    tableName = "progreso",
    foreignKeys = [
        ForeignKey(
            entity = ParadaEntity::class,
            parentColumns = ["id"],
            childColumns = ["paradaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paradaId"])]
)
data class ProgresoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val paradaId: Int,
    val estado: String = "bloqueada",
    val fechaInicio: Long? = null,
    val fechaCompletado: Long? = null,
    val puntuacion: Int = 0,
    val tiempoEmpleado: Int? = null,
    val intentos: Int = 0,
    val sincronizado: Boolean = false
)
