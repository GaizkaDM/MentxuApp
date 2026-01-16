package com.gaizkafrost.mentxuapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar el progreso del usuario en cada parada
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
    val estado: String = "bloqueada", // "bloqueada", "activa", "completada"
    val fechaInicio: Long? = null,
    val fechaCompletado: Long? = null,
    val puntuacion: Int = 0,
    val tiempoEmpleado: Int? = null, // en segundos
    val intentos: Int = 0,
    val sincronizado: Boolean = false // Para saber si ya se sincronizó con el backend
)
