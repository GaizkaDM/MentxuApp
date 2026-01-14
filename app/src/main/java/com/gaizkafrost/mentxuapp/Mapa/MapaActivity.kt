package com.gaizkafrost.mentxuapp.Mapa

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
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

import com.gaizkafrost.mentxuapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : BaseMenuActivity(), OnMapReadyCallback {
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
                // --- LÓGICA DE NAVEGACIÓN CENTRALIZADA EN MENUAUDIO ---
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
                // Si la parada está bloqueada o completada, informamos al usuario
                val mensaje = when (paradaClicada?.estado) {
                    EstadoParada.BLOQUEADA -> "Aurreko geltokia osatu behar duzu lehenago."
                    EstadoParada.COMPLETADA -> "Geltoki hau osatu duzu jada!"
                    else -> "Geltoki hau ez dago erabilgarri."
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

    override fun onMenuCreated(menu: Menu) {
        // Ocultar la opción "Mapa" ya que ya estamos en él
        menu.findItem(R.id.action_mapa)?.isVisible = false
    }
}
