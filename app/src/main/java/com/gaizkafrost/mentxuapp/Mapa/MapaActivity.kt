package com.gaizkafrost.mentxuapp.Mapa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.EstadoParada
import com.gaizkafrost.mentxuapp.Parada
import com.gaizkafrost.mentxuapp.Parada1.Huevo_Activity
import com.gaizkafrost.mentxuapp.Parada1.MenuAudio
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Esta función se llama cuando el usuario vuelve a esta actividad
     * (por ejemplo, después de terminar un juego). Es el lugar perfecto
     * para actualizar el estado visual del mapa.
     */
    override fun onResume() {
        super.onResume()
        // Si el mapa ya está inicializado, actualiza los marcadores.
        if (::mMap.isInitialized) {
            actualizarMarcadores()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        actualizarMarcadores() // Dibuja los marcadores por primera vez

        // Listener para cuando se hace clic en un marcador
        mMap.setOnMarkerClickListener { marker ->
            // Obtenemos el objeto Parada asociado al marcador
            val paradaClicada = marker.tag as? Parada

            if (paradaClicada != null && paradaClicada.estado == EstadoParada.ACTIVA) {
                // --- LÓGICA DE NAVEGACIÓN BASADA EN EL ID DE LA PARADA ---
                Toast.makeText(this, "Iniciando actividad para: ${paradaClicada.nombre}", Toast.LENGTH_SHORT).show()

                // Decide qué actividad iniciar basándose en el ID de la parada
                val intent: Intent? = when (paradaClicada.id) {
                    1 -> Intent(this, MenuAudio::class.java)
                    2 -> {
                        Toast.makeText(this, "Parada 2 no implementada.", Toast.LENGTH_SHORT).show()
                        null
                    }
                    3 -> Intent(this, com.gaizkafrost.mentxuapp.Parada3.MenuAudio3::class.java)
                    // 4 -> Intent(this, JuegoParada4::class.java)
                    // ... etc.
                    else -> {
                        Toast.makeText(this, "Juego para esta parada no implementado.", Toast.LENGTH_SHORT).show()
                        null // No hacer nada si el juego no existe
                    }
                }

                // Si se creó un intent, se le añade el ID de la parada y se lanza
                intent?.let {
                    it.putExtra("ID_PARADA", paradaClicada.id)
                    startActivity(it)
                }

            } else {
                // Si la parada está bloqueada o completada, informamos al usuario
                val mensaje = when (paradaClicada?.estado) {
                    EstadoParada.BLOQUEADA -> "Debes completar la parada anterior primero."
                    EstadoParada.COMPLETADA -> "¡Ya has completado esta parada!"
                    else -> "Esta parada no está disponible."
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
            true // Indicamos que hemos gestionado el clic
        }

        // Centrar la cámara en Santurtzi
        val santurtzi = LatLng(43.329, -3.029)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(santurtzi, 15f))
    }

    private fun actualizarMarcadores() {
        mMap.clear() // Limpiamos el mapa antes de volver a dibujar los marcadores

        val paradas = ParadasRepository.obtenerTodas()

        paradas.forEach { parada ->
            val colorIcono = when (parada.estado) {
                EstadoParada.ACTIVA -> BitmapDescriptorFactory.HUE_RED // Rojo para la activa
                EstadoParada.COMPLETADA -> BitmapDescriptorFactory.HUE_GREEN // Verde para las completadas
                EstadoParada.BLOQUEADA -> BitmapDescriptorFactory.HUE_VIOLET // Violeta para las bloqueadas
            }

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(parada.latLng)
                    .title(parada.nombre)
                    .icon(BitmapDescriptorFactory.defaultMarker(colorIcono))
            )
            marker?.tag = parada // Asociamos el objeto Parada completo al marcador
        }
    }
}
