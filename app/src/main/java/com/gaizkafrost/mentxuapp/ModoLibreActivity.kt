package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import com.gaizkafrost.mentxuapp.Parada1.SopaDeLetrasActivity
import com.gaizkafrost.mentxuapp.Parada2.DiferenciasActivity
import com.gaizkafrost.mentxuapp.Parada3.Relacionar
import com.gaizkafrost.mentxuapp.Parada4.JuegoRecogida
import com.gaizkafrost.mentxuapp.Parada5.FishingProcessActivity
import com.gaizkafrost.mentxuapp.Parada6.Parada6Activity
import com.google.android.material.card.MaterialCardView

class ModoLibreActivity : BaseMenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_libre)

        setupCards()
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
