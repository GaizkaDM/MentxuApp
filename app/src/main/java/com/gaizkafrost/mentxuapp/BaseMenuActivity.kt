package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Clase base para todas las actividades con Bottom Navigation.
 * La barra de navegación estará fija en todas las pantallas.
 */
abstract class BaseMenuActivity : AppCompatActivity() {

    protected var startTime: Long = 0
    protected open var isScoringEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isScoringEnabled) {
            startTime = System.currentTimeMillis()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        
        // Configurar Toolbar si existe
        try {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            toolbar?.let { setSupportActionBar(it) }
        } catch (e: Exception) {}

        // Configurar Bottom Navigation
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar el indicador cada vez que la actividad vuelve al frente
        updateNavigationSelection()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation) ?: return

        // Primero marcar el ítem activo SIN listener para evitar navegación
        val currentItemId = when (this) {
            is PerfilActivity -> R.id.nav_perfil
            is MapaActivity -> R.id.nav_mapa
            is RankingActivity -> R.id.nav_ranking
            is ModoLibreActivity -> R.id.nav_modo_libre
            else -> null
        }
        
        // Establecer el ítem seleccionado antes de configurar el listener
        currentItemId?.let { bottomNav.selectedItemId = it }

        // Ahora configurar el listener de navegación
        setupNavigationListener(bottomNav)
    }

    private fun updateNavigationSelection() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation) ?: return
        
        val currentItemId = when (this) {
            is PerfilActivity -> R.id.nav_perfil
            is MapaActivity -> R.id.nav_mapa
            is RankingActivity -> R.id.nav_ranking
            is ModoLibreActivity -> R.id.nav_modo_libre
            else -> return
        }
        
        // Solo actualizar si el ítem seleccionado es diferente
        if (bottomNav.selectedItemId != currentItemId) {
            // Desactivar temporalmente el listener para evitar navegación
            bottomNav.setOnItemSelectedListener(null)
            bottomNav.selectedItemId = currentItemId
            // Re-configurar el listener
            setupNavigationListener(bottomNav)
        }
    }

    private fun setupNavigationListener(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil -> {
                    if (this !is PerfilActivity) {
                        startActivity(Intent(this, PerfilActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                    }
                    true
                }
                R.id.nav_mapa -> {
                    if (this !is MapaActivity) {
                        startActivity(Intent(this, MapaActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                    }
                    true
                }
                R.id.nav_ranking -> {
                    if (this !is RankingActivity) {
                        startActivity(Intent(this, RankingActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                    }
                    true
                }
                R.id.nav_modo_libre -> {
                    if (this !is ModoLibreActivity) {
                        startActivity(Intent(this, ModoLibreActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                    }
                    true
                }
                else -> false
            }
        }
    }

    protected fun getElapsedTimeSeconds(): Int {
        val endTime = System.currentTimeMillis()
        return ((endTime - startTime) / 1000).toInt()
    }

    protected fun calculateScore(): Int {
        val secondsElapsed = getElapsedTimeSeconds()
        val score = (1000 - (secondsElapsed * 2))
        return score.coerceAtLeast(100)
    }

    protected fun showScoreResult(score: Int) {
        val intent = Intent(this, ScoreResultActivity::class.java)
        intent.putExtra("EXTRA_SCORE", score)
        startActivity(intent)
        finish()
    }
}
