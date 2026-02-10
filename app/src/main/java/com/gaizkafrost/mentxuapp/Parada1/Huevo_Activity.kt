package com.gaizkafrost.mentxuapp.Parada1

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R

class Huevo_Activity : BaseMenuActivity() {
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
            Toast.makeText(this, "Ia-ia! Jarraitu bilatzen...", Toast.LENGTH_SHORT).show()
        }

        // Sistema de pistas
        setupHint(getString(R.string.hint_huevo))
    }

    private fun huevoEncontrado(huevoEncontrado: ImageView) {
        // Mostramos un mensaje de felicitación
        Toast.makeText(this, "Aurkitu duzu! Zorionak!", Toast.LENGTH_LONG).show()


        // Hacemos visible el huevo (opcional, para resaltarlo)
        huevoEncontrado.visibility = View.VISIBLE

        // El huevo se ha encontrado, se pasará a la sopa de letras
        // IMPORTANTE: NO marcamos la parada como completada aquí porque
        // la Parada 1 tiene varios juegos. Solo la SopaDeLetrasActivity
        // (el último juego) debe marcar la parada como completada.
        val idParadaActual = intent.getIntExtra("ID_PARADA", -1)


        // Desactivamos los listeners para que no se pueda seguir jugando
        findViewById<View>(R.id.zonaClicableHuevo).setOnClickListener(null)
        findViewById<ImageView>(R.id.imagenFondo).setOnClickListener(null)

        // Opcional: Cerrar la actividad después de unos segundos para ir a la sopa de letras
        huevoEncontrado.postDelayed({
            // Lanzar la sopa de letras después de encontrar el huevo
            val intent = Intent(this, SopaDeLetrasActivity::class.java)
            intent.putExtra("ID_PARADA", idParadaActual)
            // NO usamos showScoreResult aquí porque queremos ir a la Sopa de Letras
            // Pero el usuario dijo "cada actividad". Huevo_Activity es una actividad.
            // Si muestro el resultado aquí, el botón "Continuar" me llevará al mapa.
            // Entonces no iré a la Sopa de Letras.
            // Quizás deba preguntar... o decidir.
            // Generalmente "actividad" para el usuario es el minijuego completo.
            // En Parada 1 hay dos. 
            // Si el botón de cerrar lleva al mapa, entonces Huevo_Activity no debería mostrar score si va a Sopa de Letras.
            // Pero el usuario explícitamente dijo "aparezca al terminar la actividad junto a un boton de Continuar que me lleve al mapa de nuevo".
            // Si Huevo_Activity lleva a Sopa de Letras, NO es el comportamiento esperado ir al mapa.
            // Así que para Huevo_Activity NO mostraré puntuación para no romper el flujo hacia Sopa de Letras.
            
            startActivity(intent)
            finish() // Cierra esta actividad
        }, 2000) // 2000 milisegundos = 2 segundos
    }
}
