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
                // Primero: Actualizar paradas
                val responseStops = api.obtenerParadas()
                if (responseStops.isSuccessful) {
                    responseStops.body()?.let { paradasRemote ->
                        val entities = paradasRemote.map { it.toEntity() }
                        paradaDao.insertarTodas(entities)
                        Log.d(TAG, "Paradas actualizadas")
                    }
                }

                // Segundo: Actualizar progreso del usuario (CRITICAL)
                val responseProgress = api.obtenerProgresoUsuario(usuarioId)
                if (responseProgress.isSuccessful) {
                    responseProgress.body()?.let { progressResponse ->
                        val progressEntities = progressResponse.progreso.map { it.toEntity() }
                        progresoDao.insertarTodos(progressEntities)
                        Log.d(TAG, "Progreso actualizado: ${progressEntities.size} items")
                    }
                }
                
                // Cargar datos finales combinados después de sincronizar
                val paradasLocalesActualizadas = paradaDao.obtenerTodas()
                val progresosActualizados = progresoDao.obtenerProgresoUsuario(usuarioId)
                
                val paradasConEstado = paradasLocalesActualizadas.map { entity ->
                    val progresoParada = progresosActualizados.find { it.paradaId == entity.id }
                    entity.copy(estado = progresoParada?.estado ?: "bloqueada").toParada()
                }
                emit(Resource.Success(paradasConEstado))
                Log.d(TAG, "Datos sincronizados emitidos")
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

    /**
     * Comprueba si el usuario ha completado todas las paradas (es decir, la última).
     */
    suspend fun esJuegoCompletado(usuarioId: Int): Boolean {
        return try {
            val paradas = paradaDao.obtenerTodas()
            val ultimaParada = paradas.maxByOrNull { it.orden }
            
            if (ultimaParada != null) {
                val progreso = progresoDao.obtenerProgresoPorParada(usuarioId, ultimaParada.id)
                progreso?.estado == "completada"
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al comprobar si el juego está completado", e)
            false
        }
    }

    /**
     * Obtiene todos los progresos de un usuario para mostrar puntuaciones.
     */
    suspend fun obtenerProgresosUsuario(usuarioId: Int): List<com.gaizkafrost.mentxuapp.data.local.entity.ProgresoEntity> {
        return try {
            progresoDao.obtenerProgresoUsuario(usuarioId)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener progresos del usuario", e)
            emptyList()
        }
    }

    /**
     * Obtiene el flujo de progresos del usuario para actualizaciones en tiempo real.
     */
    fun obtenerProgresoUsuarioFlow(usuarioId: Int): Flow<List<com.gaizkafrost.mentxuapp.data.local.entity.ProgresoEntity>> {
        return progresoDao.obtenerProgresoUsuarioFlow(usuarioId)
    }
    
    // ==================== LOGROS ====================
    
    /**
     * Verificar y obtener nuevos logros desbloqueados
     * Llama al backend para verificar si el usuario ha desbloqueado nuevos logros
     * 
     * @return Lista de logros recién desbloqueados, o lista vacía si no hay nuevos
     */
    suspend fun verificarLogros(usuarioId: Int): List<com.gaizkafrost.mentxuapp.data.remote.dto.LogroDesbloqueado> {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            Log.d(TAG, "Sin conexión, no se pueden verificar logros")
            return emptyList()
        }
        
        return try {
            val response = api.verificarLogros(usuarioId)
            if (response.isSuccessful) {
                val nuevosLogros = response.body()?.nuevosLogros ?: emptyList()
                Log.d(TAG, "Verificación de logros: ${nuevosLogros.size} nuevos desbloqueados")
                nuevosLogros
            } else {
                Log.w(TAG, "Error al verificar logros: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar logros", e)
            emptyList()
        }
    }
    
    /**
     * Obtener todos los logros del usuario (desbloqueados y pendientes)
     */
    suspend fun obtenerLogrosUsuario(usuarioId: Int): com.gaizkafrost.mentxuapp.data.remote.dto.LogrosUsuarioResponse? {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            return null
        }
        
        return try {
            val response = api.obtenerLogrosUsuario(usuarioId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener logros", e)
            null
        }
    }
    
    /**
     * Registrar un intento de actividad (para estadísticas avanzadas)
     */
    suspend fun registrarIntento(
        usuarioId: Int,
        paradaId: Int,
        tipoActividad: String,
        puntuacion: Int,
        tiempoSegundos: Int,
        resultado: String = "exito",
        errores: Int = 0,
        pistasUsadas: Int = 0
    ) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            Log.d(TAG, "Sin conexión, intento no registrado")
            return
        }
        
        try {
            val request = com.gaizkafrost.mentxuapp.data.remote.dto.RegistrarIntentoRequest(
                usuarioId = usuarioId,
                paradaId = paradaId,
                tipoActividad = tipoActividad,
                puntuacion = puntuacion,
                tiempoSegundos = tiempoSegundos,
                resultado = resultado,
                errores = errores,
                pistasUsadas = pistasUsadas
            )
            
            val response = api.registrarIntento(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Intento registrado: ${response.body()?.numeroIntento}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar intento", e)
        }
    }
}
