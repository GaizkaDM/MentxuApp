package com.gaizkafrost.mentxuapp.data.local.dao

import androidx.room.*
import com.gaizkafrost.mentxuapp.data.local.entity.ProgresoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con progreso del usuario
 */
@Dao
interface ProgresoDao {
    
    @Query("SELECT * FROM progreso WHERE usuarioId = :usuarioId ORDER BY paradaId ASC")
    fun obtenerProgresoUsuarioFlow(usuarioId: Int): Flow<List<ProgresoEntity>>
    
    @Query("SELECT * FROM progreso WHERE usuarioId = :usuarioId ORDER BY paradaId ASC")
    suspend fun obtenerProgresoUsuario(usuarioId: Int): List<ProgresoEntity>
    
    @Query("SELECT * FROM progreso WHERE usuarioId = :usuarioId AND paradaId = :paradaId")
    suspend fun obtenerProgresoPorParada(usuarioId: Int, paradaId: Int): ProgresoEntity?
    
    @Query("SELECT * FROM progreso WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizados(): List<ProgresoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(progreso: ProgresoEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(progresos: List<ProgresoEntity>): List<Long>
    
    @Update
    suspend fun actualizar(progreso: ProgresoEntity): Int
    
    @Query("UPDATE progreso SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Int): Int
    
    @Query("UPDATE progreso SET estado = :nuevoEstado WHERE usuarioId = :usuarioId AND paradaId = :paradaId")
    suspend fun actualizarEstado(usuarioId: Int, paradaId: Int, nuevoEstado: String): Int
    
    @Delete
    suspend fun eliminar(progreso: ProgresoEntity): Int
    
    @Query("DELETE FROM progreso WHERE usuarioId = :usuarioId")
    suspend fun eliminarProgresoUsuario(usuarioId: Int): Int
}
