package com.gaizkafrost.mentxuapp.utils

/**
 * Clase sellada para representar los estados de las operaciones asíncronas
 * Útil para manejar estados de carga, éxito y error en ViewModels
 */
/**
 * Clase sellada (Sealed Class) genérica para encapsular el estado de datos y operaciones de red.
 * Facilita el manejo de estados de carga, éxito y error en la capa de UI.
 *
 * @param T El tipo de dato que contiene este recurso.
 * @property data El dato en sí (puede ser nulo en estados de error o carga).
 * @property message Mensaje de error (solo presente en estado Error).
 *
 * @author Diego, Gaizka, Xiker
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /** Operación exitosa. Contiene los datos solicitados. */
    class Success<T>(data: T) : Resource<T>(data)

    /** Operación fallida. Contiene un mensaje de error y opcionalmente datos cacheados. */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /** Operación en curso. Puede contener datos previos para mostrar mientras se actualiza. */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
