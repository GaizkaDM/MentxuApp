package com.gaizkafrost.mentxuapp.Parada3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.R

class Parada3Imagen2 : BaseMenuActivity() {
    override var isScoringEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_parada3_imagen2)

        val nextButton: Button = findViewById(R.id.nextButton)
        val imageView: android.widget.ImageView = findViewById(R.id.imgLadoBarco)

        // Ajustar escala de la imagen rotada para que ocupe el máximo espacio sin recortar
        imageView.post {
            val viewWidth = imageView.width.toFloat()
            val viewHeight = imageView.height.toFloat()
            val drawable = imageView.drawable

            if (viewWidth > 0 && viewHeight > 0 && drawable != null) {
                val dWidth = drawable.intrinsicWidth.toFloat()
                val dHeight = drawable.intrinsicHeight.toFloat()

                if (dHeight > 0) {
                    val imageRatio = dWidth / dHeight
                    val screenRatio = viewHeight / viewWidth
                    
                    val scale = kotlin.math.min(screenRatio, imageRatio)
                    
                    imageView.scaleX = scale
                    imageView.scaleY = scale
                }
            }
        }

        nextButton.setOnClickListener {
            val idParada = intent.getIntExtra("ID_PARADA", 3)
            Intent(this, Relacionar::class.java).apply {
                putExtra("ID_PARADA", idParada)
                startActivity(this)
            }
            finish()
        }
    }
}
