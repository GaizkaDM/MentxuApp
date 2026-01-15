package com.gaizkafrost.mentxuapp.data.repository

import android.content.Context
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.data.local.database.AppDatabase
import com.gaizkafrost.mentxuapp.data.remote.api.RetrofitClient
import com.gaizkafrost.mentxuapp.utils.NetworkHelper
import com.gaizkafrost.mentxuapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import android.util.Log

/**
 * Repository mejorado para gestionar paradas con sincron ización online/offline
 */
class ParadasRepositoryMejorado(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val api = RetrofitClient.api
    private val paradaDao = database.paradaDao()
    private val progresoDao = database.progresoDao()
    
    companion object {
        private const val TAG = "ParadasRepository"
    }
    
    /**
     * Obtener todas las paradas con estrategia cache-first
     * 1. Mostrar datos locales inmediatamente (si existen)
     * 2. Intentar actualizar desde backend en segundo plano
     */
    fun obtenerParadas(usuarioId: Int): Flow<Resource<List<Parada>>> = flow {
        emit(Resource.Loading())
        
        try {
            // 1. Primero cargar de local (rápido)
            val paradasLocales = paradaDao.obtenerTodas()
            if (paradasLocales.isNotEmpty()) {
                // Obtener estados del progreso del usuario
                val progresos = progresoDao.obtenerProgresoUsuario(usuarioId)
                val paradasConEstado = paradasLocales.map { entity ->
                    val progresoParada = progresos.find { it.paradaId == entity.id }
                    entity.copy(estado = progresoParada?.estado ?: "bloqueada").toParada()
                }
                emit(Resource.Success(paradasConEstado))
                Log.d(TAG, "Paradas locales cargadas: ${paradasConEstado.size}")
            }
            
            // 2. Intentar actualizar desde backend
            if (NetworkHelper.isNetworkAvailable(context)) {
                val response = api.obtenerParadas()
                if (response.isSuccessful) {
                    response.body()?.let { paradasRemote ->
                        // Guardar en cache local
                        val entities = paradasRemote.map { it.toEntity() }
                        paradaDao.insertarTodas(entities)
                        
                        // Emitir datos frescos del servidor
                        val progresos = progresoDao.obtenerProgresoUsuario(usuarioId)
                        val paradasConEstado = entities.map { entity ->
                            val progresoParada = progresos.find { it.paradaId == entity.id }
                            entity.copy(estado = progresoParada?.estado ?: "bloqueada").toParada()
                        }
                        emit(Resource.Success(paradasConEstado))
                        Log.d(TAG, "Paradas actualizadas desde servidor: ${paradasConEstado.size}")
                    }
                } else {
                    Log.w(TAG, "Error al obtener paradas del servidor: ${response.code()}")
                    // Si ya tenemos datos locales, no emitir error
                    if (paradasLocales.isEmpty()) {
                        emit(Resource.Error("Error al obtener paradas: ${response.message()}"))
                    }
                }
            } else {
                Log.d(TAG, "Sin conexión, usando datos locales")
                // Si no hay red y no hay datos locales, error
                if (paradasLocales.isEmpty()) {
                    emit(Resource.Error("Sin conexión y sin datos locales"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener paradas", e)
            // Si ya emitimos datos locales, no mostrar error
            val paradasLocales = paradaDao.obtenerTodas()
            if (paradasLocales.isEmpty()) {
                emit(Resource.Error("Error: ${e.localizedMessage}"))
            }
        }
    }
    
    /**
     * Obtener parada activa del usuario
     */
    suspend fun obtenerParadaActiva(usuarioId: Int): Parada? {
        return try {
            val progreso = progresoDao.obtenerProgresoUsuario(usuarioId)
                .firstOrNull { it.estado == "activa" }
            
            progreso?.let {
                paradaDao.obtenerPorId(it.paradaId)?.toParada()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener parada activa", e)
            null
        }
    }
    
    /**
     * Completar una parada (guardar local y sincronizar con backend)
     */
    suspend fun completarParada(
        usuarioId: Int,
        paradaId: Int,
        puntuacion: Int = 100,
        tiempoEmpleado: Int? = null,
        intentos: Int = 1
    ): Resource<Boolean> {
        return try {
            // 1. Actualizar localmente primero (optimistic update)
            val ahora = System.currentTimeMillis()
            val progresoActual = progresoDao.obtenerProgresoPorParada(usuarioId, paradaId)
            
            if (progresoActual != null) {
                val progresoActualizado = progresoActual. copy(
                    estado = "completada",
                    fechaCompletado = ahora,
                    puntuacion = puntuacion,
                    tiempoEmpleado = tiempoEmpleado,
                    intentos = intentos,
                    sincronizado = false
                )
                progresoDao.actualizar(progresoActualizado)
                Log.d(TAG, "Parada $paradaId marcada como completada localmente")
                
                // Activar siguiente parada
                activarSiguienteParada(usuarioId, paradaId)
            }
            
            // 2. Sincronizar con backend si hay conexión
            if (NetworkHelper.isNetworkAvailable(context)) {
                val request = com.gaizkafrost.mentxuapp.data.remote.dto.CompletarParadaRequest(
                    usuarioId = usuarioId,
                    paradaId = paradaId,
                    puntuacion = puntuacion,
                    tiempoEmpleado = tiempoEmpleado,
                    intentos = intentos
                )
                
                val response = api.completarParada(request)
                if (response.isSuccessful) {
                    // Marcar como sincronizado
                    progresoActual?.let {
                        progresoDao.marcarComoSincronizado(it.id)
                    }
                    Log.d(TAG, "Parada $paradaId sincronizada con backend")
                    Resource.Success(true)
                } else {
                    Log.w(TAG, "Error al sincronizar: ${response.code()}, se sincronizará más tarde")
                    Resource.Success(true) // Guardado local exitoso
                }
            } else {
                Log.d(TAG, "Sin conexión, se sincronizará más tarde")
                Resource.Success(true) // Guardado local exitoso
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar parada", e)
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }
    
    /**
     * Activar la siguiente parada después de completar una
     */
    private suspend fun activarSiguienteParada(usuarioId: Int, paradaCompletadaId: Int) {
        try {
            val paradaCompletada = paradaDao.obtenerPorId(paradaCompletadaId)
            val siguiente = paradaDao.obtenerTodas()
                .firstOrNull { it.orden > (paradaCompletada?.orden ?: 0) }
            
            siguiente?.let {
                val progresoSiguiente = progresoDao.obtenerProgresoPorParada(usuarioId, it.id)
                if (progresoSiguiente != null && progresoSiguiente.estado == "bloqueada") {
                    val actualizado = progresoSiguiente.copy(
                        estado = "activa",
                        fechaInicio = System.currentTimeMillis()
                    )
                    progresoDao.actualizar(actualizado)
                    Log.d(TAG, "Parada ${it.id} activada")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al activar siguiente parada", e)
        }
    }
    
    /**
     * Sincronizar progreso pendiente con el backend
     */
    suspend fun sincronizarProgresosPendientes(usuarioId: Int): Int {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            return 0
        }
        
        return try {
            val noSincronizados = progresoDao.obtenerNoSincronizados()
                .filter { it.usuarioId == usuarioId && it.estado == "completada" }
            
            var sincronizados = 0
            noSincronizados.forEach { progreso ->
                val request = com.gaizkafrost.mentxuapp.data.remote.dto.CompletarParadaRequest(
                    usuarioId = progreso.usuarioId,
                    paradaId = progreso.paradaId,
                    puntuacion = progreso.puntuacion,
                    tiempoEmpleado = progreso.tiempoEmpleado,
                    intentos = progreso.intentos
                )
                
                val response = api.completarParada(request)
                if (response.isSuccessful) {
                    progresoDao.marcarComoSincronizado(progreso.id)
                    sincronizados++
                }
            }
            
            Log.d(TAG, "Sincronizados $sincronizados de ${noSincronizados.size} progresos pendientes")
            sincronizados
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar progresos", e)
            0
        }
    }
}
