package com.gaizkafrost.mentxuapp.Parada4

import android.content.Intent
import android.graphics.drawable.Animatable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.R

class MenuAudio4 : BaseMenuActivity() {
    override var isScoringEnabled = false

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playPauseButton: Button
    private lateinit var audioSeekBar: SeekBar
    private lateinit var handler: Handler
    private var gifAnimatable: Animatable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_audio4)

        // --- 1. Cargar el GIF animado ---
        val gifImageView: ImageView = findViewById(R.id.gifMentxu)

        // Crear un ImageLoader que soporte GIFs
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        // Cargar el GIF y obtener el control de la animación
        gifImageView.load(R.drawable.mentxu_habla, imageLoader) {
            listener(onSuccess = { _, result ->
                // Guarda la animación para poder controlarla
                gifAnimatable = result.drawable as? Animatable
                // Inicia el GIF en estado pausado
                gifAnimatable?.stop()
            })
        }

        // --- 2. Inicializar componentes de la UI ---
        playPauseButton = findViewById(R.id.playPauseButton)
        audioSeekBar = findViewById(R.id.audioSeekBar)
        val continueButton: Button = findViewById(R.id.continueButton)

        // --- 3. Preparar el reproductor de audio ---
        mediaPlayer = MediaPlayer.create(this, R.raw.audioa4)
        mediaPlayer?.setOnPreparedListener {
            audioSeekBar.max = it.duration
        }

        // Listener para cuando el audio termina
        mediaPlayer?.setOnCompletionListener {
            playPauseButton.text = "▶"
            gifAnimatable?.stop()
            audioSeekBar.progress = 0
        }

        handler = Handler(Looper.getMainLooper())

        // --- 4. Configurar los listeners (acciones de los botones) ---
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        continueButton.setOnClickListener {
            val idParada = intent.getIntExtra("ID_PARADA", -1)
            val intent = Intent(this, JuegoRecogida::class.java).apply {
                putExtra("ID_PARADA", idParada)
            }
            startActivity(intent)
            finish()
        }

        audioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playPauseButton.text = "▶"
                gifAnimatable?.stop() // Detiene la animación del GIF
            } else {
                it.start()
                playPauseButton.text = "❚❚"
                gifAnimatable?.start() // Inicia la animación del GIF
                updateSeekBar()
            }
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                audioSeekBar.progress = it.currentPosition
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
        gifAnimatable?.stop()
    }
}
