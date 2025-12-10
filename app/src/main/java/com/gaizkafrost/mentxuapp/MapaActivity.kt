package com.gaizkafrost.mentxuapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

        // Añadir los marcadores al mapa
        locations.forEach { (title, latLng) ->
            mMap.addMarker(MarkerOptions().position(latLng).title(title))
        }

        // Centrar la cámara en un punto intermedio de Santurtzi y ajustar el zoom
        val santurtzi = LatLng(43.329, -3.029)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(santurtzi, 15f))
    }
}
