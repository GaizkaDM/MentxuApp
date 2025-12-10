package com.gaizkafrost.mentxuapp.parada1

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.gaizkafrost.mentxuapp.R

class MenuAudio : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playPauseButton: Button
    private lateinit var audioSeekBar: SeekBar
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_audio)

        // --- 1. Cargar el GIF animado ---
        val gifImageView: ImageView = findViewById(R.id.gifMentxu)
        // Asegúrate de tener un archivo 'mentxu_habla.gif' en res/drawable
        gifImageView.load(R.drawable.mentxu_habla)

        // --- 2. Inicializar componentes de la UI ---
        playPauseButton = findViewById(R.id.playPauseButton)
        audioSeekBar = findViewById(R.id.audioSeekBar)
        val continueButton: Button = findViewById(R.id.continueButton)

        // --- 3. Preparar el reproductor de audio ---
        mediaPlayer = MediaPlayer.create(this, R.raw.audioa) // Cambia 'sonido_parada_1' por el nombre de tu audio
        mediaPlayer?.setOnPreparedListener {
            audioSeekBar.max = it.duration
        }

        handler = Handler(Looper.getMainLooper())

        // --- 4. Configurar los listeners (acciones de los botones) ---
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        continueButton.setOnClickListener {
            Toast.makeText(this, "Yendo a la siguiente actividad...", Toast.LENGTH_SHORT).show()
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
            } else {
                it.start()
                playPauseButton.text = "❚❚"
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
    }
}
