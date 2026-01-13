package com.gaizkafrost.mentxuapp.Parada2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R

class DiferenciasActivity : AppCompatActivity() {

    private lateinit var diferenciasView: DiferenciasView
    private lateinit var tvContador: TextView
    
    private val totalDiferencias = 7
    private var diferenciasEncontradas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diferencias)

        diferenciasView = findViewById(R.id.diferenciasView)
        tvContador = findViewById(R.id.tvContadorDiferencias)
        
        actualizarContador()

        // Configurar el listener para cuando se encuentre una diferencia
        diferenciasView.onDiferenciaEncontrada = {
            diferenciaEncontrada()
        }
    }

    private fun diferenciaEncontrada() {
        diferenciasEncontradas++
        actualizarContador()
        
        Toast.makeText(this, "¡Diferencia encontrada! ($diferenciasEncontradas/$totalDiferencias)", Toast.LENGTH_SHORT).show()
        
        // Verificar si se han encontrado todas las diferencias
        if (diferenciasEncontradas >= totalDiferencias) {
            juegoCompletado()
        }
    }
    
    private fun actualizarContador() {
        tvContador.text = "Diferencias encontradas: $diferenciasEncontradas/$totalDiferencias"
    }

    private fun juegoCompletado() {
        Toast.makeText(this, "¡Felicidades! Has encontrado todas las diferencias", Toast.LENGTH_LONG).show()
        
        // Marcar la parada como completada
        val idParadaActual = intent.getIntExtra("ID_PARADA", -1)
        if (idParadaActual != -1) {
            ParadasRepository.completarParada(idParadaActual)
        }
        
        // Cerrar la actividad después de 2 segundos
        diferenciasView.postDelayed({
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
