package com.gaizkafrost.mentxuapp.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gaizkafrost.mentxuapp.utils.Constants

/**
 * Manager para SharedPreferences encriptadas
 * Guarda datos sensibles del usuario de forma segura
 */
class UserPreferences(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // Usuario ID
    var userId: Int
        get() = sharedPreferences.getInt(Constants.KEY_USER_ID, -1)
        set(value) = sharedPreferences.edit().putInt(Constants.KEY_USER_ID, value).apply()
    
    // Nombre del usuario
    var userNombre: String?
        get() = sharedPreferences.getString(Constants.KEY_USER_NOMBRE, null)
        set(value) = sharedPreferences.edit().putString(Constants.KEY_USER_NOMBRE, value).apply()
    
    // Apellido del usuario
    var userApellido: String?
        get() = sharedPreferences.getString(Constants.KEY_USER_APELLIDO, null)
        set(value) = sharedPreferences.edit().putString(Constants.KEY_USER_APELLIDO, value).apply()
    
    // Device ID único
    var deviceId: String?
        get() = sharedPreferences.getString(Constants.KEY_DEVICE_ID, null)
        set(value) = sharedPreferences.edit().putString(Constants.KEY_DEVICE_ID, value).apply()
    
    // Primera vez que abre la app
    var isFirstTime: Boolean
        get() = sharedPreferences.getBoolean(Constants.KEY_FIRST_TIME, true)
        set(value) = sharedPreferences.edit().putBoolean(Constants.KEY_FIRST_TIME, value).apply()
    
    // ID de la sesión activa (para sistema de estadísticas)
    var sessionId: Int
        get() = sharedPreferences.getInt(Constants.KEY_SESSION_ID, -1)
        set(value) = sharedPreferences.edit().putInt(Constants.KEY_SESSION_ID, value).apply()
    
    // Avatar del usuario
    var userAvatar: String
        get() = sharedPreferences.getString(Constants.KEY_USER_AVATAR, "perro") ?: "perro"
        set(value) = sharedPreferences.edit().putString(Constants.KEY_USER_AVATAR, value).apply()
    
    // Color favorito del usuario
    var userColor: String
        get() = sharedPreferences.getString(Constants.KEY_USER_COLOR, "azul") ?: "azul"
        set(value) = sharedPreferences.edit().putString(Constants.KEY_USER_COLOR, value).apply()
    
    // Verificar si hay usuario logueado
    fun hasUser(): Boolean = userId > 0
    
    // Limpiar sesión
    fun clearUser() {
        sharedPreferences.edit().apply {
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USER_NOMBRE)
            remove(Constants.KEY_USER_APELLIDO)
            remove(Constants.KEY_SESSION_ID)
            remove(Constants.KEY_USER_AVATAR)
            remove(Constants.KEY_USER_COLOR)
            apply()
        }
    }
    
    // Guardar usuario completo
    fun saveUser(id: Int, nombre: String, apellido: String, avatar: String = "perro", color: String = "azul") {
        sharedPreferences.edit().apply {
            putInt(Constants.KEY_USER_ID, id)
            putString(Constants.KEY_USER_NOMBRE, nombre)
            putString(Constants.KEY_USER_APELLIDO, apellido)
            putString(Constants.KEY_USER_AVATAR, avatar)
            putString(Constants.KEY_USER_COLOR, color)
            apply()
        }
    }
}
