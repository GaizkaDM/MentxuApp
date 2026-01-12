package com.gaizkafrost.mentxuapp

import com.google.android.gms.maps.model.LatLng

object ParadasRepository {

    // Lista privada de paradas
    private val paradas = mutableListOf(
        Parada(1, "Santurtziko Udala (Mentxu)", LatLng(43.328833, -3.032944)),
        Parada(2, "“El niño y el perro” eskultura", LatLng(43.328833, -3.032306)),
        Parada(3, "Agurtza itsasontzia", LatLng(43.327000, -3.023778)),
        Parada(4, "Itsas-museoa", LatLng(43.330639, -3.030750)),
        Parada(5, "Itsas-portua", LatLng(43.330417, -3.030722)),
        Parada(6, "“Monumento niños y niñas de la guerra” eskultura", LatLng(43.330500, -3.029917))
    )

    init {
        // Al iniciar, la primera parada siempre está activa
        paradas.firstOrNull()?.estado = EstadoParada.ACTIVA
    }

    // Función para obtener todas las paradas
    fun obtenerTodas(): List<Parada> {
        return paradas
    }

    // Función para marcar una parada como completada y activar la siguiente
    fun completarParada(idParada: Int) {
        val paradaActual = paradas.find { it.id == idParada }
        paradaActual?.estado = EstadoParada.COMPLETADA

        // Buscamos la siguiente parada que esté bloqueada y la activamos
        val siguienteParada = paradas.firstOrNull { it.estado == EstadoParada.BLOQUEADA }
        siguienteParada?.estado = EstadoParada.ACTIVA
    }

    // Función para obtener la parada activa actual
    fun obtenerParadaActiva(): Parada? {
        return paradas.find { it.estado == EstadoParada.ACTIVA }
    }
}