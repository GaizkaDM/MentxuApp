package com.gaizkafrost.mentxuapp

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.utils.LocaleHelper

class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Botón Atrás
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        setupVolumeControl()
        setupLanguageControl()
        setupAccessibilityControl()
        setupCreditsControl()
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volumeSeekBar = findViewById<SeekBar>(R.id.volumeSeekBar)

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = currentVolume

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupLanguageControl() {
        val rgLanguage = findViewById<RadioGroup>(R.id.rgLanguage)
        val rbEuskara = findViewById<RadioButton>(R.id.rbEuskara)
        val rbCastellano = findViewById<RadioButton>(R.id.rbCastellano)

        val currentLang = UserPreferences(this).language
        if (currentLang == "es") {
            rbCastellano.isChecked = true
        } else {
            rbEuskara.isChecked = true
        }

        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val newLang = if (checkedId == R.id.rbCastellano) "es" else "eu"
            
            if (newLang != currentLang) {
                // Cambiar idioma y reiniciar app
                LocaleHelper.setLocale(this, newLang)
                
                // Reiniciar toda la pila de actividades para aplicar idioma
                val intent = Intent(this, Presentacion::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupAccessibilityControl() {
        // Placeholder para accesibilidad (Switch Alto Contraste)
        // Aquí podrías implementar lógica real de cambio de tema/contraste
        val switchHighContrast = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchHighContrast)
        switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            // Implementar lógica de alto contraste si se desea en el futuro
        }
    }

    private fun setupCreditsControl() {
        findViewById<TextView>(R.id.btnDesarrolladores).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.creditos_desarrolladores))
                .setMessage("MentxuApp Team:\nGaizka Frost\n...")
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }

        findViewById<TextView>(R.id.btnAyudaGeneral).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.ayuda))
                .setMessage(getString(R.string.ayudaTexto))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }
}
