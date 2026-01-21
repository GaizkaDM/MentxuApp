package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para el ranking de usuarios
 */
data class RankingItemResponse(
    val posicion: Int,
    @SerializedName("usuario_id")
    val usuarioId: Int,
    val nombre: String,
    @SerializedName("puntuacion_total")
    val puntuacionTotal: Int,
    @SerializedName("paradas_completadas")
    val paradasCompletadas: Int
)
