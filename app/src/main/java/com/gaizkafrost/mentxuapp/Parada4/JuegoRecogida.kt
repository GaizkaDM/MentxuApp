package com.gaizkafrost.mentxuapp.Parada4

import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.gaizkafrost.mentxuapp.ParadasRepository
import com.gaizkafrost.mentxuapp.R
import java.util.Random

class JuegoRecogida : AppCompatActivity() {

    private lateinit var gameContainer: ConstraintLayout
    private lateinit var player: ImageView
    private lateinit var scoreText: TextView
    private lateinit var btnContinuar: Button


    private var score = 0
    private val targetScore = 20
    private var isGameRunning = true
    private val fallingObjects = mutableListOf<ImageView>()
    private val handler = Handler(Looper.getMainLooper())
    private var screenWidth = 0
    private val random = Random()
    private var fallingSpeed = 20f // Velocidad inicial


    // Control de movimiento
    private var lastTouchX = 0f
    private var isDragging = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_recogida)

        gameContainer = findViewById(R.id.gameContainer)
        player = findViewById(R.id.playerCharacter)
        scoreText = findViewById(R.id.scoreText)
        btnContinuar = findViewById(R.id.btnContinuar)


        // Obtener ancho de pantalla
        gameContainer.post {
            screenWidth = gameContainer.width
        }

        setupControls()
        startGameLoop()
        startSpawner()
    }

    private fun setupControls() {
        // Movimiento por arrastre relativo (evita saltos)
        gameContainer.setOnTouchListener { _, event ->
            if (isGameRunning) {
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        lastTouchX = event.x
                        isDragging = true
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        if (isDragging) {
                            val deltaX = event.x - lastTouchX
                            val newX = player.x + deltaX
                            
                            // Permitir un margen para bordes visuales
                            val offset = player.width / 4f
                            player.x = newX.coerceIn(-offset, screenWidth - player.width + offset)
                            
                            lastTouchX = event.x
                        }
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        isDragging = false
                    }
                }
            }
            true
        }
    }


    // Bucle principal del juego (60 FPS aprox)
    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!isGameRunning) return

            updateFallingObjects()
            checkCollisions()


            handler.postDelayed(this, 16)
        }
    }

    private fun startGameLoop() {
        handler.post(gameRunnable)
    }

    private fun startSpawner() {
        // Generar basura cada 1.5 segundos
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isGameRunning) return
                spawnObject()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }


    private fun spawnObject() {
        val obj = ImageView(this)
        
        // Lista de diferentes tipos de basura (usando las imágenes del usuario)
        val trashTypes = listOf(
            R.drawable.platano_basura,
            R.drawable.botella_basura,
            R.drawable.bolsa_basura,
            R.drawable.lata_basura
        )
        
        // Seleccionar un tipo aleatorio
        obj.setImageResource(trashTypes[random.nextInt(trashTypes.size)])

        // Tamaño MUCHO MÁS GRANDE para que los niños lo vean mejor
        val size = 150
        val params = ConstraintLayout.LayoutParams(size, size)
        obj.layoutParams = params
        
        // Posición X aleatoria
        obj.x = random.nextFloat() * (screenWidth - size)
        obj.y = -150f // Empieza arriba fuera de pantalla

        gameContainer.addView(obj)
        fallingObjects.add(obj)
    }

    private fun updateFallingObjects() {
        val iterator = fallingObjects.iterator()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            obj.y += fallingSpeed // Velocidad de caída dinámica


            // Si sale de la pantalla por mucho (margen de seguridad para memoria)
            if (obj.y > gameContainer.height + 500) {
                gameContainer.removeView(obj)
                iterator.remove()
            }

        }
    }

    private fun checkCollisions() {
        val playerRect = Rect()
        player.getGlobalVisibleRect(playerRect)

        // Ajustar la hitbox para que sea solo la parte de abajo (donde está la cesta)
        // El personaje mide 250dp, queremos que la colisión sea en los 100dp inferiores
        playerRect.top = playerRect.bottom - 200
        playerRect.inset(60, 0) // Un poco menos de margen lateral para equilibrar


        val iterator = fallingObjects.iterator()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            val objRect = Rect()
            obj.getGlobalVisibleRect(objRect)
            objRect.inset(15, 15) // Hitbox de objeto ligeramente más grande para captura más fluida

            if (Rect.intersects(playerRect, objRect)) {
                // Colisión detectada!
                score++
                updateScore()
                
                gameContainer.removeView(obj)
                iterator.remove()

                if (score >= targetScore) {
                    winGame()
                }
            }
        }
    }

    private fun updateScore() {
        scoreText.text = "Puntuak: $score/$targetScore"
        
        // Dificultad progresiva: aumentamos velocidad cada 5 puntos
        fallingSpeed = 20f + (score / 5) * 5f
    }


    private fun winGame() {
        isGameRunning = false
        Toast.makeText(this, "Oso ondo! Itsasoa garbitu duzu.", Toast.LENGTH_LONG).show()

        // Completar la parada 4
        ParadasRepository.completarParada(4)

        // Esperar un poco antes de dejar salir al usuario (o dejar que salga él)
        // Siguiendo el requerimiento manual: No cerramos.
        scoreText.text = "GARAITUA! ($score/$targetScore)"
        scoreText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        
        // Mostrar botón de continuar
        btnContinuar.visibility = View.VISIBLE
        btnContinuar.setOnClickListener {
            finish() // Volver al mapa
        }

        // Desactivar spawns y movimiento
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        isGameRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
