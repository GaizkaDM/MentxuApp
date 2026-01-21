package com.gaizkafrost.mentxuapp.data.remote.api

import com.gaizkafrost.mentxuapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API de MentxuApp - Definición de endpoints del backend Flask
 * Base URL: http://10.0.2.2:5000/api/ (emulador) o http://TU-IP:5000/api/ (dispositivo real)
 */
interface MentxuApi {
    
    // ==================== PARADAS ====================
    
    @GET("paradas")
    suspend fun obtenerParadas(): Response<List<ParadaResponse>>
    
    @GET("paradas/{id}")
    suspend fun obtenerParada(@Path("id") id: Int): Response<ParadaResponse>
    
    @GET("paradas/{id}/estadisticas")
    suspend fun obtenerEstadisticasParada(@Path("id") id: Int): Response<EstadisticasParadaResponse>
    
    // ==================== USUARIOS ====================
    
    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body request: UsuarioRequest): Response<UsuarioResponse>
    
    @GET("usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Int): Response<UsuarioDataResponse>
    
    @GET("usuarios/{id}/progreso")
    suspend fun obtenerProgresoUsuario(@Path("id") id: Int): Response<ProgresoUsuarioResponse>
    
    @GET("ranking")
    suspend fun obtenerRanking(): Response<List<RankingItemResponse>>
    
    // ==================== PROGRESO ====================
    
    @POST("progreso/completar")
    suspend fun completarParada(@Body request: CompletarParadaRequest): Response<CompletarParadaResponse>
    
    @PUT("progreso/{id}")
    suspend fun actualizarProgreso(
        @Path("id") id: Int,
        @Body request: ActualizarProgresoRequest
    ): Response<ProgresoItemResponse>
    
    @GET("estadisticas")
    suspend fun obtenerEstadisticasGenerales(): Response<EstadisticasGeneralesResponse>
}
