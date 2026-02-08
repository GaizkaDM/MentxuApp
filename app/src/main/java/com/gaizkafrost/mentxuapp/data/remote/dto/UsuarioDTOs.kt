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
    val avatar: String? = "perro",
    @SerializedName("color_favorito")
    val colorFavorito: String? = "azul"
)

// ==================== OPCIONES VISUALES ====================

/**
 * Avatares disponibles para selección
 */
object AvatarOptions {
    val avatares = listOf(
        "perro" to "🐶",
        "gato" to "🐱",
        "conejo" to "🐰",
        "zorro" to "🦊",
        "oso" to "🐻",
        "panda" to "🐼",
        "leon" to "🦁",
        "unicornio" to "🦄"
    )
    
    fun getEmoji(avatar: String): String {
        return avatares.find { it.first == avatar }?.second ?: "🐶"
    }
}

/**
 * Colores disponibles para selección
 */
object ColorOptions {
    val colores = listOf(
        "rojo" to "#E53935",
        "azul" to "#1E88E5",
        "verde" to "#43A047",
        "amarillo" to "#FDD835",
        "morado" to "#8E24AA",
        "naranja" to "#FB8C00",
        "rosa" to "#EC407A"
    )
    
    fun getHex(color: String): String {
        return colores.find { it.first == color }?.second ?: "#1E88E5"
    }
}
