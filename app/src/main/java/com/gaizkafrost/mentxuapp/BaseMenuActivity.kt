package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity

/**
 * Clase base para todas las actividades que necesitan el menú superior.
 * Centraliza la lógica del menú para evitar duplicación de código.
 */
abstract class BaseMenuActivity : AppCompatActivity() {

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
