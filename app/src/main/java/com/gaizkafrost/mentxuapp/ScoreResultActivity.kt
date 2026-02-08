package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity

class ScoreResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_score_result)

        val score = intent.getIntExtra("EXTRA_SCORE", 0)
        
        findViewById<TextView>(R.id.tvScoreValue).text = score.toString()

        findViewById<Button>(R.id.btnRanking).setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            // Verificar si venimos del modo libre
            val isFreeMode = intent.getBooleanExtra("IS_FREE_MODE", false)
            
            val targetIntent = if (isFreeMode) {
                Intent(this, ModoLibreActivity::class.java)
            } else {
                Intent(this, MapaActivity::class.java)
            }
            
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(targetIntent)
            finish()
        }
    }
}
