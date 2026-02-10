package com.gaizkafrost.mentxuapp.Mapa

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.EstadoParada
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.Parada1.Huevo_Activity
import com.gaizkafrost.mentxuapp.Parada1.MenuAudio
import com.gaizkafrost.mentxuapp.Parada2.DiferenciasActivity
import com.gaizkafrost.mentxuapp.Parada3.Relacionar
import com.gaizkafrost.mentxuapp.Parada4.JuegoRecogida
import com.gaizkafrost.mentxuapp.Parada5.FishingProcessActivity
import com.gaizkafrost.mentxuapp.Parada6.Parada6Activity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.utils.Resource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.launch

class MapaActivity : BaseMenuActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var pointAnnotationManager: PointAnnotationManager
    
    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private lateinit var mapContainer: android.view.View
    private lateinit var congratsContainer: android.view.View
    private var paradasBackend: List<Parada> = emptyList()
    private var usandoBackend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Inicializar repository
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)

        // Inicializar vistas
        mapContainer = findViewById(R.id.mapContainer)
        congratsContainer = findViewById(R.id.congratsContainer)
        
        // Inicializar Mapbox
        // Initialize Mapbox Access Token explicitly to avoid crash
        val accessToken = getString(R.string.mapbox_access_token)
        com.mapbox.common.MapboxOptions.accessToken = accessToken

        // Create MapView programmatically
        // textureView = true fixes black screen on emulators and is safe for devices
        val mapCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.mapCard)
        // Remove any previous views to be safe
        mapCard.removeAllViews()

        val mapInitOptions = com.mapbox.maps.MapInitOptions(this, textureView = true)
        mapView = MapView(this, mapInitOptions)
        
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        mapCard.addView(mapView, params)
        
        mapboxMap = mapView.getMapboxMap()
        
        // Cargar estilo de mapa (Outdoor es bueno para turismo)
        mapboxMap.loadStyleUri(Style.OUTDOORS) {
            // Configurar gestor de anotaciones (marcadores) una vez cargado el estilo
            val annotationPlugin = mapView.annotations
            pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
            
            // Listener de clics en marcadores
            pointAnnotationManager.addClickListener { annotation ->
                onMarkerClick(annotation)
                true
            }
            
            // Cargar paradas una vez listo el mapa
            cargarParadasDesdeBackend()
        }

        // Posición inicial: Santurtzi
        val santurtzi = Point.fromLngLat(-3.029, 43.329) // Longitud, Latitud
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(santurtzi)
                .zoom(14.0)
                .build()
        )

        // Verificar estado de finalización
        checkCompletionState()
    }

    override fun onResume() {
        super.onResume()
        // Al volver al mapa, verificar estado
        if (::pointAnnotationManager.isInitialized) {
            cargarParadasDesdeBackend()
        }
        checkCompletionState()

        // Sincronizar progresos pendientes al volver al mapa (si hay internet)
        val userId = userPrefs.userId
        if (userId > 0) {
            lifecycleScope.launch {
                repository.sincronizarProgresosPendientes(userId)
            }
        }
    }

    private fun checkCompletionState() {
        val userId = userPrefs.userId
        if (userId <= 0) return

        lifecycleScope.launch {
            try {
                val isCompleted = repository.esJuegoCompletado(userId)
                if (isCompleted) {
                    showCongratsScreen()
                } else {
                    showMapScreen()
                }
            } catch (e: Exception) {
                showMapScreen()
            }
        }
    }

    private fun showCongratsScreen() {
        if (congratsContainer.visibility == android.view.View.VISIBLE) return

        mapContainer.visibility = android.view.View.GONE
        congratsContainer.visibility = android.view.View.VISIBLE

        val trophyIcon = findViewById<android.view.View>(R.id.trophyIcon)
        val congratsTitle = findViewById<android.view.View>(R.id.congratsTitle)
        val congratsMessage = findViewById<android.view.View>(R.id.congratsMessage)

        trophyIcon.scaleX = 0f
        trophyIcon.scaleY = 0f
        congratsTitle.alpha = 0f
        congratsTitle.translationY = 50f
        congratsMessage.alpha = 0f
        congratsMessage.translationY = 50f

        trophyIcon.animate()
            .scaleX(1f).scaleY(1f).setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator()).start()

        congratsTitle.animate()
            .alpha(1f).translationY(0f).setDuration(600).setStartDelay(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator()).start()

        congratsMessage.animate()
            .alpha(1f).translationY(0f).setDuration(600).setStartDelay(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
    }

    private fun showMapScreen() {
        mapContainer.visibility = android.view.View.VISIBLE
        congratsContainer.visibility = android.view.View.GONE
    }

    private fun onMarkerClick(annotation: PointAnnotation) {
        // Recuperar datos de la parada desde la propiedad "jsonObject" o similar
        // Mapbox v10 Annotations guardan datos extra en 'getData()' que es un JsonElement
        val data = annotation.getData()
        val paradaId = data?.asJsonObject?.get("id")?.asInt
        
        if (paradaId == null) return

        // Buscar la parada completa en la lista actual
        val paradas = if (usandoBackend && paradasBackend.isNotEmpty()) paradasBackend else ParadasRepository.obtenerTodas()
        val paradaClicada = paradas.find { it.id == paradaId }

        if (paradaClicada != null && paradaClicada.estado == EstadoParada.ACTIVA) {
            when (paradaClicada.id) {
                1 -> MenuAudio.navegarAParada(this@MapaActivity, 1, Huevo_Activity::class.java)
                2 -> MenuAudio.navegarAParada(this@MapaActivity, 2, DiferenciasActivity::class.java)
                3 -> MenuAudio.navegarAParada(this@MapaActivity, 3, Relacionar::class.java)
                4 -> MenuAudio.navegarAParada(this@MapaActivity, 4, JuegoRecogida::class.java)
                5 -> MenuAudio.navegarAParada(this@MapaActivity, 5, FishingProcessActivity::class.java)
                6 -> MenuAudio.navegarAParada(this@MapaActivity, 6, Parada6Activity::class.java)
                else -> Toast.makeText(this, "Geltoki honetako jokoa ez dago inplementatuta.", Toast.LENGTH_SHORT).show()
            }
        } else {
            val mensaje = when (paradaClicada?.estado) {
                EstadoParada.BLOQUEADA -> "Aurreko geltokia osatu behar duzu lehenago."
                EstadoParada.COMPLETADA -> "Geltoki hau osatu duzu jada!"
                else -> "Geltoki hau ez dago erabilgarri."
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarParadasDesdeBackend() {
        val userId = userPrefs.userId
        if (userId <= 0) {
            actualizarMarcadores() // Cargar locales
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
                                    actualizarMarcadores()
                                }
                            }
                        }
                        is Resource.Error -> {
                            usandoBackend = false
                            actualizarMarcadores()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                usandoBackend = false
                actualizarMarcadores()
            }
        }
    }

    private fun actualizarMarcadores() {
        if (!::pointAnnotationManager.isInitialized) return
        
        // Limpiar marcadores existentes
        pointAnnotationManager.deleteAll()

        val paradas = if (usandoBackend && paradasBackend.isNotEmpty()) {
            paradasBackend
        } else {
            ParadasRepository.obtenerTodas()
        }

        paradas.forEach { parada ->
            // Determinar color
            val color = when (parada.estado) {
                EstadoParada.ACTIVA -> Color.RED
                EstadoParada.COMPLETADA -> Color.GREEN
                EstadoParada.BLOQUEADA -> Color.GRAY
            }

            // Crear icono dinámicamente (Círculo con borde)
            val bitmap = crearIconoMarcador(color)

            // Crear opciones de anotación
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(parada.ubicacion) // Point(lon, lat)
                .withIconImage(bitmap)
                // Guardar ID en los datos para recuperarlo al hacer clic
                .withData(com.google.gson.JsonObject().apply { 
                    addProperty("id", parada.id) 
                })

            // Añadir al mapa
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    // Utilidad para crear un círculo de color como Bitmap
    private fun crearIconoMarcador(color: Int): Bitmap {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        // Borde blanco
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Círculo interior de color
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 6, paint) // -6px de borde
        
        return bitmap
    }
}
