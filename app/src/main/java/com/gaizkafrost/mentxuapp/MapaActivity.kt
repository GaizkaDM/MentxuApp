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

        // Código de ejemplo para verificar que funciona
        val santurtzi = LatLng(43.330, -3.033)
        mMap.addMarker(MarkerOptions().position(santurtzi).title("Marcador en Santurtzi"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(santurtzi, 15f))
    }
}
