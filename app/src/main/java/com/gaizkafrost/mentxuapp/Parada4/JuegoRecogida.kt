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
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    private var score = 0
    private val targetScore = 10
    private var isGameRunning = true
    private val fallingObjects = mutableListOf<ImageView>()
    private val handler = Handler(Looper.getMainLooper())
    private var screenWidth = 0
    private val random = Random()

    // Control de movimiento
    private var playerX = 0f
    private val moveStep = 50f
    private var isMovingLeft = false
    private var isMovingRight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_recogida)

        gameContainer = findViewById(R.id.gameContainer)
        player = findViewById(R.id.playerCharacter)
        scoreText = findViewById(R.id.scoreText)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        // Obtener ancho de pantalla
        gameContainer.post {
            screenWidth = gameContainer.width
            playerX = player.x
        }

        setupControls()
        startGameLoop()
        startSpawner()
    }

    private fun setupControls() {
        // Lógica simple de movimiento continuo mientras se pulsa
        btnLeft.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> isMovingLeft = true
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> isMovingLeft = false
            }
            true
        }

        btnRight.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> isMovingRight = true
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> isMovingRight = false
            }
            true
        }
    }

    // Bucle principal del juego (60 FPS aprox)
    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!isGameRunning) return

            movePlayer()
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
                handler.postDelayed(this, 1500)
            }
        }, 1000)
    }

    private fun movePlayer() {
        if (isMovingLeft && player.x > 0) {
            player.x -= 15 // Velocidad de movimiento
        }
        if (isMovingRight && player.x < (screenWidth - player.width)) {
            player.x += 15
        }
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
            obj.y += 10 // Velocidad de caída

            // Si sale de la pantalla por abajo, eliminar
            if (obj.y > gameContainer.height) {
                gameContainer.removeView(obj)
                iterator.remove()
            }
        }
    }

    private fun checkCollisions() {
        val playerRect = Rect()
        player.getGlobalVisibleRect(playerRect)

        // Reducir la hitbox del jugador para que sea más justo
        playerRect.inset(20, 20)

        val iterator = fallingObjects.iterator()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            val objRect = Rect()
            obj.getGlobalVisibleRect(objRect)
            objRect.inset(10, 10)

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
        scoreText.text = "Puntos: $score/$targetScore"
    }

    private fun winGame() {
        isGameRunning = false
        Toast.makeText(this, "¡Bien hecho! Has limpiado el mar.", Toast.LENGTH_LONG).show()

        // Completar la parada 4
        ParadasRepository.completarParada(4)

        // Esperar un poco antes de dejar salir al usuario (o dejar que salga él)
        // Siguiendo el requerimiento manual: No cerramos.
        scoreText.text = "¡COMPLETADO! ($score/$targetScore)"
        scoreText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        
        // Desactivar spawns y movimiento
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        isGameRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
