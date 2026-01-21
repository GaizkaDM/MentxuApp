package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.Parada1.SopaDeLetrasActivity
import com.gaizkafrost.mentxuapp.Parada2.DiferenciasActivity
import com.gaizkafrost.mentxuapp.Parada3.Relacionar
import com.gaizkafrost.mentxuapp.Parada4.JuegoRecogida
import com.gaizkafrost.mentxuapp.Parada5.FishingProcessActivity
import com.gaizkafrost.mentxuapp.Parada6.Parada6Activity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ModoLibreActivity : BaseMenuActivity() {

    private lateinit var userPrefs: UserPreferences
    private lateinit var repository: ParadasRepositoryMejorado
    
    private lateinit var lockedContainer: ConstraintLayout
    private lateinit var gamesContainer: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_libre)

        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        
        lockedContainer = findViewById(R.id.lockedContainer)
        gamesContainer = findViewById(R.id.gamesContainer)
        
        setupCards()
        setupGoToMapButton()
    }
    
    override fun onResume() {
        super.onResume()
        checkIfUnlocked()
    }
    
    private fun checkIfUnlocked() {
        val userId = userPrefs.userId
        if (userId == -1) {
            showLockedState()
            return
        }
        
        lifecycleScope.launch {
            val isCompleted = repository.esJuegoCompletado(userId)
            if (isCompleted) {
                showUnlockedState()
            } else {
                showLockedState()
            }
        }
    }
    
    private fun showLockedState() {
        lockedContainer.visibility = View.VISIBLE
        gamesContainer.visibility = View.GONE
    }
    
    private fun showUnlockedState() {
        lockedContainer.visibility = View.GONE
        gamesContainer.visibility = View.VISIBLE
    }
    
    private fun setupGoToMapButton() {
        findViewById<MaterialButton>(R.id.btnGoToMap).setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    private fun setupCards() {
        findViewById<MaterialCardView>(R.id.cardSopa).setOnClickListener {
            launchGame(SopaDeLetrasActivity::class.java, 1)
        }
        findViewById<MaterialCardView>(R.id.cardDiferencias).setOnClickListener {
            launchGame(DiferenciasActivity::class.java, 2)
        }
        findViewById<MaterialCardView>(R.id.cardRelacionar).setOnClickListener {
            launchGame(Relacionar::class.java, 3)
        }
        findViewById<MaterialCardView>(R.id.cardRecogida).setOnClickListener {
            launchGame(JuegoRecogida::class.java, 4)
        }
        findViewById<MaterialCardView>(R.id.cardPesca).setOnClickListener {
            launchGame(FishingProcessActivity::class.java, 5)
        }
        findViewById<MaterialCardView>(R.id.cardPuzzle).setOnClickListener {
            launchGame(Parada6Activity::class.java, 6)
        }
    }

    private fun launchGame(activityClass: Class<*>, idParada: Int) {
        val intent = Intent(this, activityClass)
        intent.putExtra("ID_PARADA", idParada)
        intent.putExtra("IS_FREE_MODE", true)
        startActivity(intent)
    }
}
