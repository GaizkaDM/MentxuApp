package com.gaizkafrost.mentxuapp

import com.mapbox.geojson.Point

// Enum que define el estado en el que se encuentra una parada.
// Esto ayuda a controlar si el usuario puede interactuar con ella o no.
enum class EstadoParada {
    /** La parada está bloqueada y no se puede visitar aún. */
    BLOQUEADA,

    /** La parada es la siguiente en la ruta y está lista para ser visitada. */
    ACTIVA,

    /** La parada ya ha sido completada por el usuario. */
    COMPLETADA
}

/**
 * Representa una parada dentro del recorrido turístico o educativo.
 * Contiene la información básica como su ID, nombre, ubicación y estado actual.
 *
 * @property id Identificador único de la parada.
 * @property nombre Nombre descriptivo de la parada.
 * @property ubicacion Coordenadas geográficas (Mapbox Point).
 * @property estado Estado actual de la parada (por defecto BLOQUEADA).
 * @author Diego, Gaizka, Xiker
 */
data class Parada(
    val id: Int,
    val nombre: String,
    val ubicacion: Point,
    var estado: EstadoParada = EstadoParada.BLOQUEADA
)