package com.gaizkafrost.mentxuapp.Parada1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R

class SopaDeLetrasActivity : BaseMenuActivity() {

    private lateinit var sopaDeLetrasView: SopaDeLetrasView
    private lateinit var tvPalabras: List<TextView>
    private val palabrasEncontradas = mutableSetOf<String>()
    
    private val palabrasAEncontrar = listOf(
        "ARRAUTZA", "BIZIDUNA", "HEGAZTIA", "HEGOA", 
        "KAIOA", "LUMA", "MOKOA", "MOREA", "OBIPAROA", "UDALA"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sopa_de_letras)

        sopaDeLetrasView = findViewById(R.id.sopaDeLetrasView)
        
        // Referencias a los TextViews de las palabras
        tvPalabras = listOf(
            findViewById(R.id.tvArrautza),
            findViewById(R.id.tvBiziduna),
            findViewById(R.id.tvHegaztia),
            findViewById(R.id.tvHegoa),
            findViewById(R.id.tvKaioa),
            findViewById(R.id.tvLuma),
            findViewById(R.id.tvMokoa),
            findViewById(R.id.tvMorea),
            findViewById(R.id.tvObiparoa),
            findViewById(R.id.tvUdala)
        )

        // Configurar el listener para cuando se encuentre una palabra
        sopaDeLetrasView.onPalabraEncontrada = { palabra ->
            palabraEncontrada(palabra)
        }
    }

    private fun palabraEncontrada(palabra: String) {
        if (!palabrasEncontradas.contains(palabra)) {
            palabrasEncontradas.add(palabra)
            
            Toast.makeText(this, "$palabra aurkitu duzu!", Toast.LENGTH_SHORT).show()

            
            // Marcar la palabra en verde en la lista
            val index = palabrasAEncontrar.indexOf(palabra)
            if (index != -1 && index < tvPalabras.size) {
                tvPalabras[index].setTextColor(ContextCompat.getColor(this, R.color.verde_completado))
            }
            
            // Verificar si se han encontrado todas las palabras
            if (palabrasEncontradas.size == palabrasAEncontrar.size) {
                juegoCompletado()
            }
        }
    }

    private fun juegoCompletado() {
        Toast.makeText(this, "Zorionak! Hizki-sopa osatu duzu", Toast.LENGTH_LONG).show()

        
        // Marcar la parada como completada
        val idParadaActual = intent.getIntExtra("ID_PARADA", -1)
        if (idParadaActual != -1) {
            ParadasRepository.completarParada(idParadaActual)
        }
        
        // Mostrar puntuación y cerrar la actividad después de 2 segundos
        sopaDeLetrasView.postDelayed({
            showScoreResult(calculateScore())
        }, 2000)
    }
}
