package com.gaizkafrost.mentxuapp

import com.mapbox.geojson.Point

/**
 * Repositorio encargado de gestionar la lista de paradas y su estado.
 * Funciona como una fuente de verdad única para el progreso del recorrido.
 *
 * @author Diego, Gaizka, Xiker
 */
object ParadasRepository {

    // Lista privada de paradas con sus datos iniciales
    private val paradas = mutableListOf(
        Parada(1, "Santurtziko Udala (Mentxu)", Point.fromLngLat(-3.032944, 43.328833)),
        Parada(2, "“El niño y el perro” eskultura", Point.fromLngLat(-3.032306, 43.328833)),
        Parada(3, "Agurtza itsasontzia", Point.fromLngLat(-3.023778, 43.327000)),
        Parada(4, "Itsas-museoa", Point.fromLngLat(-3.030750, 43.330639)),
        Parada(5, "Itsas-portua", Point.fromLngLat(-3.030722, 43.330417)),
        Parada(6, "“Monumento niños y niñas de la guerra” eskultura", Point.fromLngLat(-3.029917, 43.330500))
    )

    init {
        // Al iniciar, aseguramos que la primera parada sea la activa
        paradas.firstOrNull()?.estado = EstadoParada.ACTIVA
    }

    /**
     * Devuelve la lista completa de paradas con su estado actual.
     * @return Lista de objetos [Parada].
     */
    fun obtenerTodas(): List<Parada> {
        return paradas
    }

    /**
     * Marca una parada como completada y desbloquea la siguiente en la secuencia.
     *
     * Si se completa la última parada (ID 6), no se activa ninguna nueva parada,
     * dando paso al "modo libre".
     *
     * @param idParada El ID de la parada que se ha completado.
     */
    fun completarParada(idParada: Int) {
        val paradaActual = paradas.find { it.id == idParada }
        paradaActual?.estado = EstadoParada.COMPLETADA

        // Control de flujo: si no es la última, activamos la siguiente
        if (idParada != 6) {
            val siguienteParada = paradas.firstOrNull { it.estado == EstadoParada.BLOQUEADA }
            siguienteParada?.estado = EstadoParada.ACTIVA
        }
    }

    /**
     * Busca y devuelve la parada que se encuentra actualmente activa (en curso).
     * @return La [Parada] activa, o null si no hay ninguna.
     */
    fun obtenerParadaActiva(): Parada? {
        return paradas.find { it.estado == EstadoParada.ACTIVA }
    }
}