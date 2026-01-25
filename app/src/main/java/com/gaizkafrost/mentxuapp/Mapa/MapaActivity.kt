package com.gaizkafrost.mentxuapp.Mapa

import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.EstadoParada
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.Parada1.Huevo_Activity
import com.gaizkafrost.mentxuapp.Parada1.MenuAudio
import com.gaizkafrost.mentxuapp.Parada2.DiferenciasActivity
import com.gaizkafrost.mentxuapp.Parada5.FishingProcessActivity
import com.gaizkafrost.mentxuapp.Parada6.Parada6Activity
import com.gaizkafrost.mentxuapp.Parada4.JuegoRecogida
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.utils.Resource
import com.gaizkafrost.mentxuapp.R
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapaActivity : BaseMenuActivity() {
    private lateinit var mMapView: MapView
    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private lateinit var mapContainer: android.view.View
    private lateinit var congratsContainer: android.view.View
    private var paradasBackend: List<Parada> = emptyList()
    private var usandoBackend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cargar configuración de osmdroid (necesario antes de inflar el layout)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        
        setContentView(R.layout.activity_mapa)

        // Inicializar repository
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)

        // Inicializar vistas
        mapContainer = findViewById(R.id.mapContainer)
        congratsContainer = findViewById(R.id.congratsContainer)
        mMapView = findViewById(R.id.map)

        setupMap()

        // Cargar paradas desde backend PostgreSQL
        cargarParadasDesdeBackend()
        
        // Verificar estado de finalización
        checkCompletionState()
    }

    private fun setupMap() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
        mMapView.setMultiTouchControls(true)
        
        val mapController = mMapView.controller
        mapController.setZoom(15.0)
        
        // Centrar la cámara en Santurtzi
        val santurtzi = GeoPoint(43.329, -3.029)
        mapController.setCenter(santurtzi)
        
        actualizarMarcadores()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        // Al volver al mapa, verificar estado
        cargarParadasDesdeBackend()
        checkCompletionState()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
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
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        congratsTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

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

    private fun cargarParadasDesdeBackend() {
        val userId = userPrefs.userId
        if (userId <= 0) return

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
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                usandoBackend = false
            }
        }
    }

    private fun actualizarMarcadores() {
        mMapView.overlays.clear()

        val paradas = if (usandoBackend && paradasBackend.isNotEmpty()) {
            paradasBackend
        } else {
            ParadasRepository.obtenerTodas()
        }

        paradas.forEach { parada ->
            val marker = Marker(mMapView)
            marker.position = GeoPoint(parada.latitud, parada.longitud)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = parada.nombre
            
            // Personalizar icono según estado
            val colorRes = when (parada.estado) {
                EstadoParada.ACTIVA -> android.R.color.holo_red_light
                EstadoParada.COMPLETADA -> android.R.color.holo_green_light
                EstadoParada.BLOQUEADA -> android.R.color.darker_gray
            }
            
            // Usar el icono por defecto de osmdroid y tintarlo
            val icon = marker.icon
            if (icon != null) {
                val tintedIcon = DrawableCompat.wrap(icon).mutate()
                DrawableCompat.setTint(tintedIcon, ContextCompat.getColor(this, colorRes))
                marker.icon = tintedIcon
            }

            marker.setOnMarkerClickListener { m, _ ->
                if (parada.estado == EstadoParada.ACTIVA) {
                    when (parada.id) {
                        1 -> MenuAudio.navegarAParada(this, 1, Huevo_Activity::class.java)
                        2 -> MenuAudio.navegarAParada(this, 2, DiferenciasActivity::class.java)
                        3 -> MenuAudio.navegarAParada(this, 3, com.gaizkafrost.mentxuapp.Parada3.Relacionar::class.java)
                        4 -> MenuAudio.navegarAParada(this, 4, JuegoRecogida::class.java)
                        5 -> MenuAudio.navegarAParada(this, 5, FishingProcessActivity::class.java)
                        6 -> MenuAudio.navegarAParada(this, 6, Parada6Activity::class.java)
                        else -> Toast.makeText(this, "Jokoa ez dago inplementatuta.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val mensaje = when (parada.estado) {
                        EstadoParada.BLOQUEADA -> "Aurreko geltokia osatu behar duzu lehenago."
                        EstadoParada.COMPLETADA -> "Geltoki hau osatu duzu jada!"
                        else -> "Geltoki hau ez dago erabilgarri."
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            mMapView.overlays.add(marker)
        }
        
        mMapView.invalidate()
    }
}
