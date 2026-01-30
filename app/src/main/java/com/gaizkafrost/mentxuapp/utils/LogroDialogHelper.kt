package com.gaizkafrost.mentxuapp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import com.gaizkafrost.mentxuapp.R
import com.gaizkafrost.mentxuapp.data.remote.dto.LogroDesbloqueado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Helper para mostrar dialogs de logros desbloqueados
 */
object LogroDialogHelper {
    
    /**
     * Muestra un dialog de logro desbloqueado
     * 
     * @param context Contexto de la actividad
     * @param logro Datos del logro desbloqueado
     * @param onDismiss Callback cuando se cierra el dialog
     */
    fun mostrarLogroDesbloqueado(
        context: Context,
        logro: LogroDesbloqueado,
        onDismiss: () -> Unit = {}
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logro_desbloqueado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        
        // Configurar contenido
        dialog.findViewById<TextView>(R.id.tv_icono_logro)?.text = logro.icono ?: "🏆"
        dialog.findViewById<TextView>(R.id.tv_nombre_logro)?.text = logro.nombre
        dialog.findViewById<TextView>(R.id.tv_descripcion_logro)?.text = logro.descripcion ?: ""
        dialog.findViewById<TextView>(R.id.tv_puntos_logro)?.text = logro.puntos.toString()
        
        // Botón de continuar
        dialog.findViewById<Button>(R.id.btn_continuar)?.setOnClickListener {
            dialog.dismiss()
            onDismiss()
        }
        
        dialog.setOnDismissListener {
            onDismiss()
        }
        
        dialog.show()
    }
    
    /**
     * Muestra una cola de logros desbloqueados uno por uno
     * 
     * @param context Contexto de la actividad
     * @param logros Lista de logros a mostrar
     * @param onAllDismissed Callback cuando se han mostrado todos
     */
    fun mostrarLogrosEnCola(
        context: Context,
        logros: List<LogroDesbloqueado>,
        onAllDismissed: () -> Unit = {}
    ) {
        if (logros.isEmpty()) {
            onAllDismissed()
            return
        }
        
        var indiceActual = 0
        
        fun mostrarSiguiente() {
            if (indiceActual < logros.size) {
                val logro = logros[indiceActual]
                indiceActual++
                
                // Pequeña pausa entre logros
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    mostrarLogroDesbloqueado(context, logro) {
                        mostrarSiguiente()
                    }
                }
            } else {
                onAllDismissed()
            }
        }
        
        mostrarSiguiente()
    }
}
