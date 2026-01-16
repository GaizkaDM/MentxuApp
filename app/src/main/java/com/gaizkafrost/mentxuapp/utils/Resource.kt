package com.gaizkafrost.mentxuapp.utils

/**
 * Clase sellada para representar los estados de las operaciones asíncronas
 * Útil para manejar estados de carga, éxito y error en ViewModels
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
