package com.gaizkafrost.mentxuapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // 1. Busca el fragmento del mapa en el layout.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // 2. Inicia la carga del mapa. Esto hará que onMapReady() se ejecute.
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Lista de lugares para añadir marcadores
        val locations = listOf(
            "Itsas-museoa" to LatLng(43.330639, -3.030750),
            "Itsas-portua" to LatLng(43.330417, -3.030722),
            "Agurtza itsasontzia" to LatLng(43.327000, -3.023778),
            "Santurtziko Udala (Mentxu)" to LatLng(43.328833, -3.032944),
            "“El niño y el perro” eskultura" to LatLng(43.328833, -3.032306),
            "“Monumento niños y niñas de la guerra” eskultura" to LatLng(43.330500, -3.029917)
        )

        // Lista de colores para los marcadores
        val colors = listOf(
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_AZURE
        )

        // Añadir los marcadores al mapa con colores diferentes
        locations.forEachIndexed { index, (title, latLng) ->
            val markerColor = colors[index % colors.size] // Cicla a través de los colores
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
        }

        // Centrar la cámara en un punto intermedio de Santurtzi y ajustar el zoom
        val santurtzi = LatLng(43.329, -3.029)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(santurtzi, 15f))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mapa -> {
                // Regresar al mapa (reiniciar actividad o simplemente cerrar si ya estamos en ella)
                val intent = Intent(this, MapaActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            R.id.action_irten -> {
                finishAffinity() // Cierra la aplicación por completo
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
