package com.gaizkafrost.mentxuapp.Parada1

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R

class SopaDeLetrasActivity : AppCompatActivity() {

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
            
            Toast.makeText(this, "¡Has encontrado: $palabra!", Toast.LENGTH_SHORT).show()
            
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
        Toast.makeText(this, "¡Felicidades! Has completado la sopa de letras", Toast.LENGTH_LONG).show()
        
        // Marcar la parada como completada
        val idParadaActual = intent.getIntExtra("ID_PARADA", -1)
        if (idParadaActual != -1) {
            ParadasRepository.completarParada(idParadaActual)
        }
        
        // Cerrar la actividad después de 2 segundos
        sopaDeLetrasView.postDelayed({
            finish()
        }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mapa -> {
                val intent = Intent(this, MapaActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            R.id.action_irten -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
