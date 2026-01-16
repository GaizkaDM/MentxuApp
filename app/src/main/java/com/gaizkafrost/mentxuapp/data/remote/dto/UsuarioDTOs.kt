package com.gaizkafrost.mentxuapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== REQUEST ====================

data class UsuarioRequest(
    val nombre: String,
    val apellido: String,
    @SerializedName("device_id")
    val deviceId: String
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
    val deviceId: String?
)
