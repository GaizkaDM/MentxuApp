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
        setContentView(R.layout.activity_score_result)

        val score = intent.getIntExtra("EXTRA_SCORE", 0)
        
        findViewById<TextView>(R.id.tvScoreValue).text = score.toString()

        findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            // Regresar al mapa limpiando el stack
            val intent = Intent(this, MapaActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}
