package com.gaizkafrost.mentxuapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.adapter.AvatarAdapter
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.remote.api.RetrofitClient
import com.gaizkafrost.mentxuapp.data.remote.dto.RegistrarSesionRequest
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

        // Si ya hay un usuario registrado, registrar sesión e ir al mapa
        if (userPrefs.hasUser()) {
            // Registrar sesión en background
            registrarSesionUsuario(userPrefs.userId)
            
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

    // Variable para almacenar el avatar seleccionado actualmente
    private var currentSelectedAvatar: AvatarAdapter.AvatarItem? = null

    private fun mostrarDialogoDeRegistro() {
        // Crear diálogo personalizado con layout compacto
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_registro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        // Referencias a elementos del layout
        val editTextNombre: EditText = dialog.findViewById(R.id.editTextNombre)
        val editTextApellido: EditText = dialog.findViewById(R.id.editTextApellido)
        val cardAvatarSelector: CardView = dialog.findViewById(R.id.cardAvatarSelector)
        val imgSelectedAvatar: ImageView = dialog.findViewById(R.id.imgSelectedAvatar)
        val btnCancelar: Button = dialog.findViewById(R.id.btnCancelar)
        val btnConfirmar: Button = dialog.findViewById(R.id.btnConfirmar)

        // Inicializar avatar por defecto
        val avatars = AvatarAdapter.getDefaultAvatars()
        currentSelectedAvatar = avatars.firstOrNull()
        currentSelectedAvatar?.let { imgSelectedAvatar.setImageResource(it.resourceId) }

        // Click en avatar abre el selector de avatares
        cardAvatarSelector.setOnClickListener {
            mostrarSelectorDeAvatar { selectedAvatar ->
                currentSelectedAvatar = selectedAvatar
                imgSelectedAvatar.setImageResource(selectedAvatar.resourceId)
            }
        }

        // Configurar selectores de color con CardViews
        var selectedColor = "azul"
        val colorCards = mapOf(
            R.id.cardColorRojo to "rojo",
            R.id.cardColorAzul to "azul",
            R.id.cardColorVerde to "verde",
            R.id.cardColorAmarillo to "amarillo",
            R.id.cardColorMorado to "morado",
            R.id.cardColorNaranja to "naranja"
        )

        fun updateColorSelection(selectedId: Int) {
            colorCards.keys.forEach { id ->
                val card = dialog.findViewById<CardView>(id)
                if (id == selectedId) {
                    card.cardElevation = 12f
                    card.scaleX = 1.15f
                    card.scaleY = 1.15f
                    selectedColor = colorCards[id] ?: "azul"
                } else {
                    card.cardElevation = 4f
                    card.scaleX = 1.0f
                    card.scaleY = 1.0f
                }
            }
        }

        // Configurar click listeners para colores
        colorCards.keys.forEach { id ->
            dialog.findViewById<CardView>(id).setOnClickListener {
                updateColorSelection(id)
            }
        }

        // Seleccionar azul por defecto
        updateColorSelection(R.id.cardColorAzul)

        // Botones
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            val nombre = editTextNombre.text.toString().trim()
            val apellido = editTextApellido.text.toString().trim()
            val avatar = currentSelectedAvatar?.id ?: "mentxu_default"

            if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                btnConfirmar.isEnabled = false
                registrarUsuario(nombre, apellido, avatar, selectedColor, dialog)
            } else {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    /**
     * Muestra el diálogo selector de avatar en una ventana emergente separada
     */
    private fun mostrarSelectorDeAvatar(onAvatarSelected: (AvatarAdapter.AvatarItem) -> Unit) {
        val avatarDialog = Dialog(this)
        avatarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        avatarDialog.setContentView(R.layout.dialog_avatar_picker)
        avatarDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        avatarDialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        avatarDialog.setCancelable(true)

        val recyclerAvatars: RecyclerView = avatarDialog.findViewById(R.id.recyclerAvatars)
        val btnSelectAvatar: Button = avatarDialog.findViewById(R.id.btnSelectAvatar)

        val avatars = AvatarAdapter.getDefaultAvatars()
        var tempSelectedAvatar = currentSelectedAvatar ?: avatars.firstOrNull()

        val avatarAdapter = AvatarAdapter(avatars) { avatar, _ ->
            tempSelectedAvatar = avatar
        }

        // Configurar grid con 3 columnas
        recyclerAvatars.layoutManager = GridLayoutManager(this, 3)
        recyclerAvatars.adapter = avatarAdapter

        // Pre-seleccionar el avatar actual
        val currentIndex = avatars.indexOfFirst { it.id == currentSelectedAvatar?.id }
        if (currentIndex >= 0) {
            avatarAdapter.setSelectedPosition(currentIndex)
        }

        btnSelectAvatar.setOnClickListener {
            tempSelectedAvatar?.let { avatar ->
                onAvatarSelected(avatar)
            }
            avatarDialog.dismiss()
        }

        avatarDialog.show()
    }

    /**
     * Versión simple del diálogo de registro (fallback)
     */
    private fun mostrarDialogoDeRegistroSimple() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.ventana_emergente, null)
        val editTextNombre: EditText = dialogView.findViewById(R.id.editTextText)
        val editTextApellido: EditText = dialogView.findViewById(R.id.editTextText2)
        
        var selectedColor = "azul"
        var selectedAvatar = "mentxu_default"

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
                    botonComenzar.isEnabled = false
                    registrarUsuario(nombre, apellido, selectedAvatar, selectedColor, dialog)
                } else {
                    Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    /**
     * Registra al usuario en el backend Flask (para Dialog personalizado)
     */
    private fun registrarUsuario(nombre: String, apellido: String, avatar: String, color: String, dialog: Dialog) {
        registrarUsuarioInterno(nombre, apellido, avatar, color) { success ->
            if (success) {
                dialog.dismiss()
            }
        }
    }

    /**
     * Registra al usuario en el backend Flask (para AlertDialog)
     */
    private fun registrarUsuario(nombre: String, apellido: String, avatar: String, color: String, dialog: AlertDialog) {
        registrarUsuarioInterno(nombre, apellido, avatar, color) { success ->
            if (success) {
                dialog.dismiss()
            } else {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }
        }
    }

    /**
     * Lógica interna de registro de usuario
     */
    private fun registrarUsuarioInterno(
        nombre: String, 
        apellido: String, 
        avatar: String, 
        color: String, 
        onComplete: (Boolean) -> Unit
    ) {
        // Generar device ID único
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch {
            try {
                // Mostrar toast de "Registrando..."
                Toast.makeText(this@Presentacion, "Erregistratzen...", Toast.LENGTH_SHORT).show()

                val api = RetrofitClient.api
                val request = UsuarioRequest(nombre, apellido, deviceId, avatar, color)
                val response = api.registrarUsuario(request)

                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        // Guardar usuario en SharedPreferences encriptadas
                        userPrefs.saveUser(
                            id = userResponse.usuario.id,
                            nombre = nombre,
                            apellido = apellido,
                            avatar = avatar,
                            color = color
                        )
                        userPrefs.deviceId = deviceId
                        userPrefs.isFirstTime = false

                        // Registrar sesión en el backend
                        registrarSesionUsuario(userResponse.usuario.id)

                        // Mostrar éxito
                        Toast.makeText(
                            this@Presentacion,
                            "Ongi etorri $nombre! 🎉",
                            Toast.LENGTH_LONG
                        ).show()

                        // Cargar paradas
                        cargarParadasIniciales()

                        // Callback de éxito
                        onComplete(true)

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
                        "Errorea erregistratzean: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete(false)
                }
            } catch (e: Exception) {
                // Error de conexión
                Toast.makeText(
                    this@Presentacion,
                    "Konexio errorea: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                onComplete(false)
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

    /**
     * Registra una nueva sesión de usuario en el backend
     * Esto permite trackear estadísticas de uso de la app
     */
    private fun registrarSesionUsuario(usuarioId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val deviceInfo = android.os.Build.MODEL + " - Android " + android.os.Build.VERSION.RELEASE
                val request = RegistrarSesionRequest(
                    usuarioId = usuarioId,
                    tipoDispositivo = "android",
                    deviceInfo = deviceInfo
                )
                
                val response = api.registrarSesion(request)
                
                if (response.isSuccessful) {
                    response.body()?.let { sesionResponse ->
                        // Guardar el ID de sesión para poder cerrarla después
                        userPrefs.sessionId = sesionResponse.sesion.id
                        android.util.Log.d("Presentacion", "Sesión registrada: ${sesionResponse.sesion.id}")
                    }
                } else {
                    android.util.Log.e("Presentacion", "Error al registrar sesión: ${response.code()}")
                }
            } catch (e: Exception) {
                // No mostrar error al usuario, solo loguear
                android.util.Log.e("Presentacion", "Error de conexión al registrar sesión: ${e.message}")
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
}