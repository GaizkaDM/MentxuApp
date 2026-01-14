package com.gaizkafrost.mentxuapp.Parada1

import android.content.Intent
import android.graphics.drawable.Animatable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.R

class MenuAudio : BaseMenuActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playPauseButton: Button
    private lateinit var audioSeekBar: SeekBar
    private lateinit var tvExplicacion: TextView
    private lateinit var handler: Handler
    private var gifAnimatable: Animatable? = null

    companion object {
        private const val EXTRA_ID_PARADA = "EXTRA_ID_PARADA"
        private const val EXTRA_SIGUIENTE_CLASE = "EXTRA_SIGUIENTE_CLASE"

        /**
         * Método estático para navegar al menú de audio de forma sencilla.
         * @param idParada El número de la parada (para cargar audioaX y textoExplicacionX).
         * @param siguienteActividad La clase de la actividad que se abrirá al pulsar Continuar.
         */
        fun navegarAParada(context: android.content.Context, idParada: Int, siguienteActividad: Class<*>) {
            val intent = Intent(context, MenuAudio::class.java).apply {
                putExtra(EXTRA_ID_PARADA, idParada)
                putExtra(EXTRA_SIGUIENTE_CLASE, siguienteActividad.name)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_audio)

        val idParada = intent.getIntExtra(EXTRA_ID_PARADA, 1)
        val siguienteClaseName = intent.getStringExtra(EXTRA_SIGUIENTE_CLASE)

        // --- 1. Cargar el GIF animado ---
        val gifImageView: ImageView = findViewById(R.id.gifMentxu)
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        gifImageView.load(R.drawable.mentxu_habla, imageLoader) {
            listener(onSuccess = { _, result ->
                gifAnimatable = result.drawable as? Animatable
                gifAnimatable?.stop()
            })
        }

        // --- 2. Inicializar componentes de la UI ---
        playPauseButton = findViewById(R.id.playPauseButton)
        audioSeekBar = findViewById(R.id.audioSeekBar)
        tvExplicacion = findViewById(R.id.tvExplicacion)
        val continueButton: Button = findViewById(R.id.continueButton)

        // --- 3. Cargar Texto y Audio Dinámicamente ---
        configurarRecursos(idParada)

        // --- 4. Configurar listeners ---
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        continueButton.setOnClickListener {
            if (siguienteClaseName != null) {
                try {
                    val targetClass = Class.forName(siguienteClaseName)
                    val intent = Intent(this, targetClass).apply {
                        putExtra("ID_PARADA", idParada)
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Errorea hurrengo jarduera irekitzean", Toast.LENGTH_SHORT).show()
                }

            } else {
                finish()
            }
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

        handler = Handler(Looper.getMainLooper())
    }

    private fun configurarRecursos(idParada: Int) {
        // Cargar Texto dinámicamente: R.string.textoExplicacionX
        val textoResId = resources.getIdentifier("textoExplicacion$idParada", "string", packageName)
        if (textoResId != 0) {
            tvExplicacion.setText(textoResId)
        } else {
            tvExplicacion.text = "Ez da azalpenik aurkitu $idParada geltokirako"
        }


        // Cargar Audio dinámicamente: R.raw.audioaX
        val audioResId = resources.getIdentifier("audioa$idParada", "raw", packageName)
        if (audioResId != 0) {
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.setOnPreparedListener {
                audioSeekBar.max = it.duration
            }
            mediaPlayer?.setOnCompletionListener {
                playPauseButton.text = "▶"
                gifAnimatable?.stop()
                audioSeekBar.progress = 0
            }
        } else {
            Toast.makeText(this, "Ez da audiorik aurkitu $idParada geltokirako", Toast.LENGTH_SHORT).show()
        }

    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playPauseButton.text = "▶"
                gifAnimatable?.stop()
            } else {
                it.start()
                playPauseButton.text = "❚❚"
                gifAnimatable?.start()
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
