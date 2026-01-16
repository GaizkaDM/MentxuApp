package com.gaizkafrost.mentxuapp.data.local.dao

import androidx.room.*
import com.gaizkafrost.mentxuapp.data.local.entity.ParadaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con paradas
 */
@Dao
interface ParadaDao {
    
    @Query("SELECT * FROM paradas ORDER BY orden ASC")
    fun obtenerTodasFlow(): Flow<List<ParadaEntity>>
    
    @Query("SELECT * FROM paradas ORDER BY orden ASC")
    suspend fun obtenerTodas(): List<ParadaEntity>
    
    @Query("SELECT * FROM paradas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ParadaEntity?
    
    @Query("SELECT * FROM paradas WHERE estado = :estado ORDER BY orden ASC")
    suspend fun obtenerPorEstado(estado: String): List<ParadaEntity>
    
    @Query("SELECT * FROM paradas WHERE estado = 'activa' LIMIT 1")
    suspend fun obtenerParadaActiva(): ParadaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(parada: ParadaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(paradas: List<ParadaEntity>)
    
    @Update
    suspend fun actualizar(parada: ParadaEntity)
    
    @Query("UPDATE paradas SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)
    
    @Delete
    suspend fun eliminar(parada: ParadaEntity)
    
    @Query("DELETE FROM paradas")
    suspend fun eliminarTodas()
    
    @Query("SELECT COUNT(*) FROM paradas")
    suspend fun contarParadas(): Int
    
    @Query("SELECT COUNT(*) FROM paradas WHERE estado = 'completada'")
    suspend fun contarCompletadas(): Int
}
