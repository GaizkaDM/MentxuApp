package com.gaizkafrost.mentxuapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gaizkafrost.mentxuapp.data.local.dao.ParadaDao
import com.gaizkafrost.mentxuapp.data.local.dao.ProgresoDao
import com.gaizkafrost.mentxuapp.data.local.entity.ParadaEntity
import com.gaizkafrost.mentxuapp.data.local.entity.ProgresoEntity

/**
 * Base de datos local de la aplicación usando Room
 * Versión 1 con 2 tablas: paradas y progreso
 */
@Database(
    entities = [ParadaEntity::class, ProgresoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun paradaDao(): ParadaDao
    abstract fun progresoDao(): ProgresoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mentxu_database"
                )
                    .fallbackToDestructiveMigration() // Solo para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
