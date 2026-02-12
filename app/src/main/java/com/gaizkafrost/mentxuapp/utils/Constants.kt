package com.gaizkafrost.mentxuapp.utils

/**
 * Objeto que almacena constantes globales utilizadas en toda la aplicación.
 * Centraliza claves de SharedPreferences, endpoints de API y configuraciones generales.
 *
 * @author Diego, Gaizka, Xiker
 */
object Constants {
    
    // --- SharedPreferences Keys ---
    /** Nombre del archivo de preferencias compartidas. */
    const val PREFS_NAME = "mentxu_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NOMBRE = "user_nombre"
    const val KEY_USER_APELLIDO = "user_apellido"
    const val KEY_DEVICE_ID = "device_id"
    const val KEY_FIRST_TIME = "first_time"
    const val KEY_SESSION_ID = "session_id"
    const val KEY_USER_AVATAR = "user_avatar"
    const val KEY_USER_COLOR = "user_color"
    
    // --- API Endpoints ---
    const val ENDPOINT_PARADAS = "paradas"
    const val ENDPOINT_USUARIOS = "usuarios"
    const val ENDPOINT_PROGRESO = "progreso"
    
    // --- Estados de Parada ---
    const val ESTADO_BLOQUEADA = "bloqueada"
    const val ESTADO_ACTIVA = "activa"
    const val ESTADO_COMPLETADA = "completada"
    
    // --- Configuración y Timeouts ---
    const val NETWORK_TIMEOUT = 30_000L // 30 segundos
    const val SYNC_INTERVAL_HOURS = 6L // Sincronización cada 6 horas
    
    // --- WorkManager Tags ---
    const val WORK_TAG_SYNC = "sync_work"
    
    // --- Canales de Notificación ---
    const val NOTIFICATION_CHANNEL_ID = "mentxu_channel"
    const val NOTIFICATION_CHANNEL_NAME = "MentxuApp Notifications"
} 
