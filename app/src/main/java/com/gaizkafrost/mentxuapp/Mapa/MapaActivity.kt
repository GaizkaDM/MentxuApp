package com.gaizkafrost.mentxuapp.Mapa

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.EstadoParada
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.Parada1.Huevo_Activity
import com.gaizkafrost.mentxuapp.Parada1.MenuAudio
import com.gaizkafrost.mentxuapp.Parada2.DiferenciasActivity
import com.gaizkafrost.mentxuapp.Parada5.FishingProcessActivity
import com.gaizkafrost.mentxuapp.Parada6.Parada6Activity
import com.gaizkafrost.mentxuapp.Parada4.JuegoRecogida
import com.gaizkafrost.mentxuapp.Parada4.MenuAudio4

import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.utils.Resource

import com.gaizkafrost.mentxuapp.R
import com.gaizkafrost.mentxuapp.RankingActivity
import com.gaizkafrost.mentxuapp.BuildConfig
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Bitmap
import androidx.core.content.ContextCompat

class MapaActivity : BaseMenuActivity() {
    private lateinit var mapView: MapView
    private var pointAnnotationManager: PointAnnotationManager? = null
    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private lateinit var mapContainer: android.view.View
    private lateinit var congratsContainer: android.view.View
    private var paradasBackend: List<Parada> = emptyList()
    private var usandoBackend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Configurar el token de Mapbox antes de inflar la vista
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        // Inicializar repository
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        // Inicializar vistas
        mapContainer = findViewById(R.id.mapContainer)
        congratsContainer = findViewById(R.id.congratsContainer)
        mapView = findViewById(R.id.mapView)
        
        // Cargar el estilo y configurar el mapa
        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            // El estilo está cargado
            inicializarGestorAnotaciones()
            actualizarMarcadores()
        }
        // Centrar la cámara en Santurtzi
        val santurtzi = Point.fromLngLat(-3.029, 43.329)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(santurtzi)
                .zoom(14.0)
                .build()
        )
        // Cargar paradas desde backend PostgreSQL
        cargarParadasDesdeBackend()
        
        // Verificar estado de finalización
        checkCompletionState()
    }

    /**
     * Esta función se llama cuando el usuario vuelve a esta actividad
     * (por ejemplo, después de terminar un juego). Es el lugar perfecto
     * para actualizar el estado visual del mapa.
     */
    override fun onResume() {
        super.onResume()
        // Al volver al mapa, verificar estado
        cargarParadasDesdeBackend()
        checkCompletionState()
    }

    private fun checkCompletionState() {
        val userId = userPrefs.userId
        if (userId <= 0) return

        lifecycleScope.launch {
            try {
                // Verificar si se completó todo el juego
                // Usamos la misma lógica que en Modo Libre
                val isCompleted = repository.esJuegoCompletado(userId)
                
                if (isCompleted) {
                    showCongratsScreen()
                } else {
                    showMapScreen()
                }
            } catch (e: Exception) {
                // En caso de error, mostramos el mapa por defecto
                showMapScreen()
            }
        }
    }

    private fun showCongratsScreen() {
        if (congratsContainer.visibility == android.view.View.VISIBLE) return

        mapContainer.visibility = android.view.View.GONE
        congratsContainer.visibility = android.view.View.VISIBLE

        // Referencias a las vistas dentro del container
        val trophyIcon = findViewById<android.view.View>(R.id.trophyIcon)
        val congratsTitle = findViewById<android.view.View>(R.id.congratsTitle)
        val congratsMessage = findViewById<android.view.View>(R.id.congratsMessage)

        // Estado inicial para la animación (ocultos o pequeños)
        trophyIcon.scaleX = 0f
        trophyIcon.scaleY = 0f
        congratsTitle.alpha = 0f
        congratsTitle.translationY = 50f
        congratsMessage.alpha = 0f
        congratsMessage.translationY = 50f

        // 1. Animar Trofeo (Efecto rebote)
        trophyIcon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // 2. Animar Título (Fade in + Subir) con retraso
        congratsTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // 3. Animar Mensaje (Fade in + Subir) con más retraso
        congratsMessage.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    private fun showMapScreen() {
        mapContainer.visibility = android.view.View.VISIBLE
        congratsContainer.visibility = android.view.View.GONE
    }

    private fun inicializarGestorAnotaciones() {
        if (pointAnnotationManager == null) {
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
            
            pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                val paradaClicada = annotation.getData()?.asJsonObject?.let { json ->
                    // Reconstruir objeto parada desde los datos guardados en la anotación si es necesario
                    // Pero aquí podemos usar el ID que guardamos
                    val id = json.get("id")?.asInt
                    paradasBackend.find { it.id == id } ?: ParadasRepository.obtenerTodas().find { it.id == id }
                }

                if (paradaClicada != null && paradaClicada.estado == EstadoParada.ACTIVA) {
                    when (paradaClicada.id) {
                        1 -> MenuAudio.navegarAParada(this@MapaActivity, 1, Huevo_Activity::class.java)
                        2 -> MenuAudio.navegarAParada(this@MapaActivity, 2, DiferenciasActivity::class.java)
                        3 -> MenuAudio.navegarAParada(this@MapaActivity, 3, com.gaizkafrost.mentxuapp.Parada3.Relacionar::class.java)
                        4 -> MenuAudio.navegarAParada(this@MapaActivity, 4, JuegoRecogida::class.java)
                        5 -> MenuAudio.navegarAParada(this@MapaActivity, 5, FishingProcessActivity::class.java)
                        6 -> MenuAudio.navegarAParada(this@MapaActivity, 6, Parada6Activity::class.java)
                        else -> {
                            Toast.makeText(this, "Geltoki honetako jokoa ez dago inplementatuta.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val mensaje = when (paradaClicada?.estado) {
                        EstadoParada.BLOQUEADA -> "Aurreko geltokia osatu behar duzu lehenago."
                        EstadoParada.COMPLETADA -> "Geltoki hau osatu duzu jada!"
                        else -> "Geltoki hau ez dago erabilgarri."
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
                true
            })
        }
    }

    /**
     * Intenta cargar paradas desde el backend
     * Si falla, usa las paradas locales hardcodeadas
     */
    private fun cargarParadasDesdeBackend() {
        val userId = userPrefs.userId
        if (userId <= 0) {
            // No hay usuario, usar paradas locales
            return
        }

        lifecycleScope.launch {
            try {
                repository.obtenerParadas(userId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { paradas ->
                                if (paradas.isNotEmpty()) {
                                    paradasBackend = paradas
                                    usandoBackend = true
                                    
                                    // Actualizar mapa si ya está listo
                                    if (::mapView.isInitialized) {
                                        actualizarMarcadores()
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            // Error: usar paradas locales
                            usandoBackend = false
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                // Cualquier error: usar paradas locales
                usandoBackend = false
            }
        }
    }

    private fun actualizarMarcadores() {
        val manager = pointAnnotationManager ?: return
        manager.deleteAll()

        // Usar paradas del backend si están disponibles, si no usar locales
        val paradas = if (usandoBackend && paradasBackend.isNotEmpty()) {
            paradasBackend
        } else {
            ParadasRepository.obtenerTodas()
        }

        paradas.forEach { parada ->
            // Mapbox usa iconos por nombre en el estilo o bitmaps externos
            // Por simplicidad usaremos un marcador por defecto si no hay iconos cargados
            // Pero para seguir el esquema de colores, creamos PointAnnotationOptions
            
            val point = Point.fromLngLat(parada.longitud, parada.latitud)
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("id", parada.id)

            // Definir color según estado
            val color = when (parada.estado) {
                EstadoParada.ACTIVA -> android.graphics.Color.RED
                EstadoParada.COMPLETADA -> android.graphics.Color.GREEN
                EstadoParada.BLOQUEADA -> android.graphics.Color.MAGENTA
            }

            val bitmap = crearBitmapMarcador(color)

            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withData(jsonObject)
                .withIconImage(bitmap)

            manager.create(pointAnnotationOptions)
        }
    }

    /**
     * Crea un Bitmap circular simple para usar como marcador
     */
    private fun crearBitmapMarcador(color: Int): Bitmap {
        val size = 60
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        // Dibujar sombra/borde blanco
        paint.color = android.graphics.Color.WHITE
        paint.isAntiAlias = true
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Dibujar círculo de color
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
        
        return bitmap
    }
}
