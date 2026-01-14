package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity

/**
 * Clase base para todas las actividades que necesitan el menú superior.
 * Centraliza la lógica del menú para evitar duplicación de código.
 * 
 * IMPORTANTE: Si tu layout tiene un Toolbar con id="toolbar", se configurará automáticamente.
 * De lo contrario, se usará el ActionBar por defecto.
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
        
        // Intentar configurar un Toolbar si existe en el layout
        try {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            toolbar?.let {
                setSupportActionBar(it)
            }
        } catch (e: Exception) {
            // Si no hay toolbar en el layout, se usará el ActionBar por defecto
        }
    }

    /**
     * Calcula la puntuación basada en el tiempo transcurrido.
     * Puntuación = max(100, 1000 - (segundos * 2))
     */
    protected fun calculateScore(): Int {
        val endTime = System.currentTimeMillis()
        val secondsElapsed = (endTime - startTime) / 1000
        val score = (1000 - (secondsElapsed * 2)).toInt()
        return score.coerceAtLeast(100)
    }

    /**
     * Lanza la pantalla de resultados con la puntuación calculada.
     */
    protected fun showScoreResult(score: Int) {
        val intent = Intent(this, ScoreResultActivity::class.java)
        intent.putExtra("EXTRA_SCORE", score)
        startActivity(intent)
        finish()
    }

    /**
     * Override this method in child activities if you need to customize menu visibility.
     * For example, MapaActivity hides the action_mapa item.
     */
    protected open fun onMenuCreated(menu: Menu) {
        // Default implementation: do nothing
        // Child classes can override to customize menu items
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        menu?.let { onMenuCreated(it) }
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
