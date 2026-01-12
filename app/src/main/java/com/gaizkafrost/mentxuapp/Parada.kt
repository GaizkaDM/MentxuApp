package com.gaizkafrost.mentxuapp

import com.google.android.gms.maps.model.LatLng

// 1. Enum para definir los estados posibles de una parada
enum class EstadoParada {
    BLOQUEADA, // Gris, no se puede hacer clic
    ACTIVA,    // Rojo, es la siguiente parada a visitar
    COMPLETADA // Verde (o un color vivo), ya ha sido visitada
}

// 2. Clase de datos para representar una parada
data class Parada(
    val id: Int,
    val nombre: String,
    val latLng: LatLng,
    var estado: EstadoParada = EstadoParada.BLOQUEADA // Por defecto, todas están bloqueadas
)