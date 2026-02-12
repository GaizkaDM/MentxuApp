package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Actividad base abstracta que gestiona la navegación inferior (BottomNavigation) y funcionalidades comunes.
 * Todas las actividades principales (Mapa, Perfil, Ranking, Modo Libre) heredan de esta clase.
 *
 * Funcionalidades incluidas:
 * - Configuración y gestión de la barra de navegación inferior.
 * - Sistema de puntuación base (cálculo de tiempo y errores).
 * - Sistema de pistas (Hint System) con diálogo emergente.
 * - Soporte multiidioma (Context Wrapper).
 *
 * @author Diego, Gaizka, Xiker
 */
abstract class BaseMenuActivity : AppCompatActivity() {

    protected var startTime: Long = 0
    protected open var isScoringEnabled: Boolean = true
    private var hintText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isScoringEnabled) {
            startTime = System.currentTimeMillis()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        
        // Ocultar Toolbar si existe (evitar franja morada)
        try {
            val toolbar = findViewById<android.view.View>(R.id.toolbar)
            if (toolbar != null) {
                toolbar.visibility = android.view.View.GONE
            }
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

    protected var errorCount: Int = 0

    protected fun addError() {
        errorCount++
    }

    protected fun calculateScore(): Int {
        val secondsElapsed = getElapsedTimeSeconds()
        // Fórmula: 1000 base - (2 puntos por segundo) - (50 puntos por fallo)
        val score = (1000 - (secondsElapsed * 2) - (errorCount * 50))
        return score.coerceAtLeast(100) // Mínimo 100 puntos siempre
    }

    protected fun showScoreResult(score: Int) {
        val intent = Intent(this, ScoreResultActivity::class.java)
        intent.putExtra("EXTRA_SCORE", score)
        intent.putExtra("EXTRA_ERRORS", errorCount) // Pasamos errores por si quieres mostrarlos
        intent.putExtra("EXTRA_TIME", getElapsedTimeSeconds())
        
        // Propagar el flag de modo libre si existe en la actividad actual
        val isFreeMode = this.intent.getBooleanExtra("IS_FREE_MODE", false)
        intent.putExtra("IS_FREE_MODE", isFreeMode)
        
        startActivity(intent)
        finish()
    }

    // --- SISTEMA DE PISTAS ---

    private var hintButton: android.widget.ImageView? = null

    /**
     * Configura una pista para la actividad actual.
     * Muestra una imagen estática en la esquina superior derecha que, al pulsarse, abre el diálogo de ayuda.
     */
    protected fun setupHint(text: String) {
        this.hintText = text
        
        // Si ya existe el botón, solo actualizamos el comportamiento (aunque el texto ya se guardó)
        if (hintButton != null) return

        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content)
        
        hintButton = android.widget.ImageView(this).apply {
            setImageResource(R.drawable.logo) // Usamos el logo como icono
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            
            // Definir tamaño en píxeles (aprox 60dp)
            val size = (60 * resources.displayMetrics.density).toInt()
            val marginTop = (16 * resources.displayMetrics.density).toInt()
            val marginRight = 0 // Pegado totalmente a la derecha
            
            val params = android.widget.FrameLayout.LayoutParams(size, size).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                topMargin = marginTop
                rightMargin = marginRight
            }
            layoutParams = params
            
            setOnClickListener { showHintDialog() }
            
            // Asegurar que esté por encima de todo
            elevation = 10f
        }
        
        rootView.addView(hintButton)
    }

    private fun showHintDialog() {
        hintText?.let { text ->
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.ayuda, null)
            
            val tvAyuda = dialogView.findViewById<TextView>(R.id.ayudaTexto)
            tvAyuda.text = text
            
            val dialog = builder.setView(dialogView).create()
            
            dialogView.findViewById<View>(R.id.cerrar).setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        }
    }
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.gaizkafrost.mentxuapp.utils.LocaleHelper.onAttach(newBase))
    }



}
