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
                repository.obtenerParadas(userPrefs.userId).collect { resource ->
                    when (resource) {
                        is com.gaizkafrost.mentxuapp.utils.Resource.Success -> {
                            val paradas = resource.data ?: emptyList()
                            val completadas = paradas.count { it.estado == EstadoParada.COMPLETADA }
                            
                            findViewById<TextView>(R.id.tvParadasCompletadas).text = completadas.toString()
                            // Calcular puntuación aproximada (100 puntos por parada)
                            findViewById<TextView>(R.id.tvPuntuacionTotal).text = (completadas * 100).toString()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                findViewById<TextView>(R.id.tvParadasCompletadas).text = "0"
                findViewById<TextView>(R.id.tvPuntuacionTotal).text = "0"
            }
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
