package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import kotlinx.coroutines.launch

class PerfilActivity : BaseMenuActivity() {

    private lateinit var userPrefs: UserPreferences
    private lateinit var repository: ParadasRepositoryMejorado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_perfil)

        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)

        // Mostrar datos del usuario
        findViewById<TextView>(R.id.tvNombre).text = userPrefs.userNombre ?: "Usuario"
        findViewById<TextView>(R.id.tvApellido).text = userPrefs.userApellido ?: ""

        // Cargar avatar del usuario
        cargarAvatarUsuario()

        // Cargar estadísticas
        cargarEstadisticas()

        // Botón cerrar sesión
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            userPrefs.clearUser()
            val intent = Intent(this, Presentacion::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        // Botón ajustes
        findViewById<android.widget.ImageButton>(R.id.btnSettings).setOnClickListener {
            // Abrir la nueva actividad de ajustes
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarEstadisticas() {
        lifecycleScope.launch {
            try {
                repository.obtenerProgresoUsuarioFlow(userPrefs.userId).collect { progresos ->
                    val completadas = progresos.count { it.estado == "completada" }
                    val puntuacionTotal = progresos.sumOf { it.puntuacion }
                    
                    findViewById<TextView>(R.id.tvParadasCompletadas).text = completadas.toString()
                    findViewById<TextView>(R.id.tvPuntuacionTotal).text = puntuacionTotal.toString()
                }
            } catch (e: Exception) {
                findViewById<TextView>(R.id.tvParadasCompletadas).text = "0"
                findViewById<TextView>(R.id.tvPuntuacionTotal).text = "0"
            }
        }
        
        // También disparamos obtenerParadas para forzar la sincronización inicial con el servidor
        lifecycleScope.launch {
            repository.obtenerParadas(userPrefs.userId).collect { /* Solo para sincronizar */ }
        }
    }

    /**
     * Carga el avatar del usuario basado en su preferencia guardada
     */
    private fun cargarAvatarUsuario() {
        val avatarId = userPrefs.userAvatar
        val avatarResource = getAvatarDrawable(avatarId)
        findViewById<ImageView>(R.id.ivAvatar).setImageResource(avatarResource)
    }

    /**
     * Convierte el ID del avatar guardado al recurso drawable correspondiente
     */
    private fun getAvatarDrawable(avatarId: String): Int {
        return when (avatarId) {
            "mentxu_default" -> R.drawable.mentxu_victoria
            "mentxu_bombera" -> R.drawable.mentxu_bombera
            "mentxu_mecanica" -> R.drawable.mentxu_mecanica
            "mentxu_medica" -> R.drawable.mentxu_medica
            "mentxu_policia" -> R.drawable.mentxu_policia
            "mentxu_profesor" -> R.drawable.mentxu_profesor
            else -> R.drawable.mentxu_victoria // Por defecto
        }
    }
}
