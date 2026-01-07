package com.gaizkafrost.mentxuapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Clase Presentacion que representa la actividad inicial de la aplicación.
 * Permite al usuario introducir sus datos y acceder a la actividad Mapa.
 *
 * @author Intissar, Aketza (Refactorizado)
 * @version 1.7
 */
class Presentacion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentacion)

        // Inicializamos las vistas y configuramos sus listeners directamente
        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            mostrarDialogoDeRegistro()
        }

        findViewById<ImageView>(R.id.ayuda).setOnClickListener {
            mostrarDialogoDeAyuda()
        }

        findViewById<ImageView>(R.id.credenciales).setOnClickListener {
            mostrarCredenciales()
        }
    }

    private fun mostrarDialogoDeRegistro() {
        // Usamos el inflador de la actividad que ya está disponible
        val dialogView = LayoutInflater.from(this).inflate(R.layout.ventana_emergente, null)
        val editTextNombre: EditText = dialogView.findViewById(R.id.editTextText)
        val editTextApellido: EditText = dialogView.findViewById(R.id.editTextText2)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_comenzar), null) // Se anula el listener para validación
            .setNegativeButton(getString(R.string.dialog_cancelar)) { d, _ -> d.dismiss() }
            .create()

        // Validamos los campos antes de cerrar el diálogo
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = editTextNombre.text.toString().trim()
                val apellido = editTextApellido.text.toString().trim()

                if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                    // ✅ CAMBIO: Se comenta la navegación a 'Mapa' para evitar el error.
                    // Cuando crees la actividad 'Mapa.kt', solo tienes que descomentar estas líneas.

                    val intent = Intent(this, MapaActivity::class.java).apply {
                        putExtra("nombre", nombre)
                        putExtra("apellido", apellido)
                    }
                    startActivity(intent)


                    // Mostramos un mensaje temporal para confirmar que funciona
                    Toast.makeText(this, "Usuario registrado: $nombre $apellido", Toast.LENGTH_LONG).show()

                    dialog.dismiss()
                } else {
                    Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoDeAyuda() {
        val ayudaView = layoutInflater.inflate(R.layout.ayuda, null)
        val textoAyuda: TextView = ayudaView.findViewById(R.id.ayudaTexto)
        textoAyuda.text = getString(R.string.ayudaTexto)

        val dialog = AlertDialog.Builder(this)
            .setView(ayudaView)
            .create()

        ayudaView.findViewById<Button>(R.id.cerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarCredenciales() {
        // Usar saltos de línea en el archivo de strings es más limpio
        val mensaje = """
            ${getString(R.string.desarrolladores)}
            Gaizka Rodriguez
            Xiker García
            Diego Fernandez

            ${getString(R.string.clientes)}
            Maialen Ascasibar
            Rita Barbosa
            Xabier González
            Leizuri Lombera
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.credenciales))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.ok)) { d, _ -> d.dismiss() }
            .show()
    }
}