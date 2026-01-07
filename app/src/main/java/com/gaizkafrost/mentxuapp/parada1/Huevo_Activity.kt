package com.gaizkafrost.mentxuapp.parada1

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R

class Huevo_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La línea enableEdgeToEdge() puede dar problemas con algunos layouts.
        // Si la UI se ve rara, prueba a comentarla.
        // enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.activity_huevo)

        // No necesitas el código de insets si usas un layout simple como este.
        // Lo he comentado para simplificar.
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        // 1. Obtenemos las referencias a nuestras vistas del layout
        val zonaClicable: View = findViewById(R.id.zonaClicableHuevo)
        val imagenFondo: ImageView = findViewById(R.id.imagenFondo)
        val huevoEncontrado: ImageView = findViewById(R.id.huevoEncontrado) // Opcional

        // 2. Configuramos el listener en el área invisible
        zonaClicable.setOnClickListener {
            huevoEncontrado(huevoEncontrado) // Llamamos a la función de éxito
        }

        // 3. Listener en la imagen de fondo para cuando el usuario falla
        imagenFondo.setOnClickListener {
            // Damos una pista o simplemente indicamos que ha fallado
            Toast.makeText(this, "¡Casi! Sigue buscando...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun huevoEncontrado(huevoEncontrado: ImageView) {
        // Mostramos un mensaje de felicitación
        Toast.makeText(this, "¡Lo has encontrado! ¡Felicidades!", Toast.LENGTH_LONG).show()

        // Hacemos visible el huevo (opcional, para resaltarlo)
        huevoEncontrado.visibility = View.VISIBLE

        // Obtenemos el ID de la parada que se estaba jugando (necesitarás pasarlo desde MapaActivity)
        val idParadaActual = intent.getIntExtra("ID_PARADA", -1)
        if (idParadaActual != -1) {
            // Actualizamos el estado de la parada en el repositorio
            ParadasRepository.completarParada(idParadaActual)
        }

        // Desactivamos los listeners para que no se pueda seguir jugando
        findViewById<View>(R.id.zonaClicableHuevo).setOnClickListener(null)
        findViewById<ImageView>(R.id.imagenFondo).setOnClickListener(null)

        // Opcional: Cerrar la actividad después de unos segundos para volver al mapa
        huevoEncontrado.postDelayed({
            finish() // Cierra esta actividad y vuelve a MapaActivity
        }, 2000) // 2000 milisegundos = 2 segundos
    }
}
