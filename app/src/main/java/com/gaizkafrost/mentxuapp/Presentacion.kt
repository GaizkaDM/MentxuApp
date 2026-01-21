package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.remote.api.RetrofitClient
import com.gaizkafrost.mentxuapp.data.remote.dto.UsuarioRequest
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Clase Presentacion que representa la actividad inicial de la aplicación.
 * Permite al usuario introducir sus datos, registrarlos en el backend y acceder al mapa.
 *
 * @author Diego, Gaizka (Backend Integration)
 * @version 2.0
 */
class Presentacion : BaseMenuActivity() {

    private lateinit var userPrefs: UserPreferences
    private lateinit var repository: ParadasRepositoryMejorado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentacion)

        // Inicializar UserPreferences y Repository
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)

        // Si ya hay un usuario registrado, ir directamente al mapa
        if (userPrefs.hasUser()) {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar listeners
        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            mostrarDialogoDeRegistro()
        }

        findViewById<ImageView>(R.id.ayuda).setOnClickListener {
            mostrarDialogoDeAyuda()
        }

        findViewById<ImageView>(R.id.credenciales).setOnClickListener {
            mostrarCredenciales()
        }
    }

    private fun mostrarDialogoDeRegistro() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.ventana_emergente, null)
        val editTextNombre: EditText = dialogView.findViewById(R.id.editTextText)
        val editTextApellido: EditText = dialogView.findViewById(R.id.editTextText2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_comenzar), null)
            .setNegativeButton(getString(R.string.dialog_cancelar)) { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val botonComenzar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonComenzar.setOnClickListener {
                val nombre = editTextNombre.text.toString().trim()
                val apellido = editTextApellido.text.toString().trim()

                if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                    // Deshabilitar botón mientras se registra
                    botonComenzar.isEnabled = false
                    
                    // Registrar usuario en el backend
                    registrarUsuario(nombre, apellido, dialog)
                } else {
                    Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    /**
     * Registra al usuario en el backend Flask
     */
    private fun registrarUsuario(nombre: String, apellido: String, dialog: AlertDialog) {
        // Generar device ID único
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch {
            try {
                // Mostrar toast de "Registrando..."
                Toast.makeText(this@Presentacion, "Registrando usuario...", Toast.LENGTH_SHORT).show()

                val api = RetrofitClient.api
                val request = UsuarioRequest(nombre, apellido, deviceId)
                val response = api.registrarUsuario(request)

                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        // Guardar usuario en SharedPreferences encriptadas
                        userPrefs.saveUser(
                            id = userResponse.usuario.id,
                            nombre = nombre,
                            apellido = apellido
                        )
                        userPrefs.deviceId = deviceId
                        userPrefs.isFirstTime = false

                        // Cerrar diálogo
                        dialog.dismiss()

                        // Mostrar éxito
                        Toast.makeText(
                            this@Presentacion,
                            "¡Registro exitoso! Bienvenido $nombre",
                            Toast.LENGTH_LONG
                        ).show()

                        // Cargar paradas (opcional, el mapa las cargará también)
                        cargarParadasIniciales()

                        // Abrir mapa
                        val intent = Intent(this@Presentacion, MapaActivity::class.java).apply {
                            putExtra("nombre", nombre)
                            putExtra("apellido", apellido)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // Error del servidor
                    Toast.makeText(
                        this@Presentacion,
                        "Error al registrar: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            } catch (e: Exception) {
                // Error de conexión
                Toast.makeText(
                    this@Presentacion,
                    "Error de conexión: ${e.localizedMessage}\n\nAsegúrate que el backend esté corriendo",
                    Toast.LENGTH_LONG
                ).show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }
        }
    }

    /**
     * Carga las paradas iniciales desde el backend/cache
     */
    private fun cargarParadasIniciales() {
        val userId = userPrefs.userId
        if (userId <= 0) return

        lifecycleScope.launch {
            repository.obtenerParadas(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Paradas cargadas correctamente
                        android.util.Log.d("Presentacion", "Paradas cargadas: ${resource.data?.size}")
                    }
                    is Resource.Error -> {
                        android.util.Log.e("Presentacion", "Error al cargar paradas: ${resource.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun mostrarDialogoDeAyuda() {
        val ayudaView = layoutInflater.inflate(R.layout.ayuda, null)
        val textoAyuda: TextView = ayudaView.findViewById(R.id.ayudaTexto)
        textoAyuda.text = getString(R.string.ayudaTexto)

        val dialog = AlertDialog.Builder(this)
            .setView(ayudaView)
            .create()

        ayudaView.findViewById<Button>(R.id.cerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarCredenciales() {
        val mensaje = """
            ${getString(R.string.desarrolladores)}
            Gaizka Rodriguez
            Xiker García
            Diego Fernandez

            ${getString(R.string.clientes)}
            Maialen Ascasibar
            Rita Barbosa
            Xabier González
            Leizuri Lombera
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.credenciales))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.ok)) { d, _ -> d.dismiss() }
            .show()
    }

    override fun onMenuCreated(menu: Menu) {
        // Ocultar la opción "Mapa" en la pantalla de registro/presentación
        menu.findItem(R.id.action_mapa)?.isVisible = false
    }
}