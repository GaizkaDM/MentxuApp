package com.gaizkafrost.mentxuapp.Parada2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.R
import kotlinx.coroutines.launch

class DiferenciasActivity : BaseMenuActivity() {

    private lateinit var diferenciasView: DiferenciasView
    private lateinit var tvContador: TextView
    
    private val totalDiferencias = 7
    private var diferenciasEncontradas = 0

    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private var idParadaActual: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diferencias)

        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        idParadaActual = intent.getIntExtra("ID_PARADA", 2)

        diferenciasView = findViewById(R.id.diferenciasView)
        tvContador = findViewById(R.id.tvContadorDiferencias)
        
        actualizarContador()

        // Configurar el listener para cuando se encuentre una diferencia
        diferenciasView.onDiferenciaEncontrada = {
            diferenciaEncontrada()
        }

        // Configurar listener para errores (toques en zonas incorrectas)
        diferenciasView.onToqueErroneo = {
            addError()
            Toast.makeText(this, "Hori ez da desberdintasuna! (-50 puntu)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun diferenciaEncontrada() {
        diferenciasEncontradas++
        actualizarContador()
        
        Toast.makeText(this, "Desberdintasuna aurkituta! ($diferenciasEncontradas/$totalDiferencias)", Toast.LENGTH_SHORT).show()

        
        // Verificar si se han encontrado todas las diferencias
        if (diferenciasEncontradas >= totalDiferencias) {
            juegoCompletado()
        }
    }
    
    private fun actualizarContador() {
        tvContador.text = "Aurkitutako desberdintasunak: $diferenciasEncontradas/$totalDiferencias"
    }


    private fun juegoCompletado() {
        Toast.makeText(this, "Zorionak! Desberdintasun guztiak aurkitu dituzu", Toast.LENGTH_LONG).show()

        
        // Marcar la parada como completada en el Backend y Local
        val userId = userPrefs.userId
        val score = calculateScore()
        val timeSpent = getElapsedTimeSeconds()
        
        lifecycleScope.launch {
            val isFreeMode = intent.getBooleanExtra("IS_FREE_MODE", false)
            if (!isFreeMode) {
                repository.completarParada(userId, idParadaActual, score, timeSpent)
            }
            
            // Mostrar puntuación y cerrar la actividad después de un breve retraso
            diferenciasView.postDelayed({
                showScoreResult(score)
            }, 1500)
        }
    }
}
