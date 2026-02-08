package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== REQUEST ====================

data class UsuarioRequest(
    val nombre: String,
    val apellido: String,
    @SerializedName("device_id")
    val deviceId: String,
    val avatar: String = "perro",
    @SerializedName("color_favorito")
    val colorFavorito: String = "azul"
)

// ==================== RESPONSE ====================

data class UsuarioResponse(
    val mensaje: String?,
    val usuario: UsuarioDataResponse
)

data class UsuarioDataResponse(
    val id: Int,
    val nombre: String,
    val apellido: String,
    @SerializedName("fecha_registro")
    val fechaRegistro: String?,
    @SerializedName("device_id")
    val deviceId: String?,
    val avatar: String?,
    @SerializedName("color_favorito")
    val colorFavorito: String?
)
