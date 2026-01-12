package com.gaizkafrost.mentxuapp.Parada2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.gaizkafrost.mentxuapp.R

class DiferenciasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var imagenOriginal: Bitmap? = null
    private var imagenDiferencias: Bitmap? = null
    private var imageRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Coordenadas de las diferencias (relativas a la imagen, de 0.0 a 1.0)
    // Áreas ampliadas para facilitar el toque
    private val diferencias = listOf(
        RectF(0.02f, 0.08f, 0.18f, 0.22f),   // Bandera del faro (esquina superior izquierda)
        RectF(0.15f, 0.12f, 0.35f, 0.25f),   // Gaviotas en el cielo
        RectF(0.65f, 0.45f, 0.82f, 0.58f),   // Ventana del edificio
        RectF(0.35f, 0.68f, 0.48f, 0.82f),   // Perro en el muelle
        RectF(0.78f, 0.70f, 0.92f, 0.85f),   // Pescador con gorro
        RectF(0.48f, 0.05f, 0.65f, 0.18f),   // Nube con forma diferente
        RectF(0.20f, 0.55f, 0.35f, 0.68f)    // Área del agua con color diferente
    )
    
    private val diferenciasEncontradas = mutableSetOf<Int>()
    
    var onDiferenciaEncontrada: (() -> Unit)? = null

    init {
        // Cargar las imágenes
        imagenOriginal = BitmapFactory.decodeResource(resources, R.drawable.santurtzi_original)
        imagenDiferencias = BitmapFactory.decodeResource(resources, R.drawable.santurtzi_diferencias)
        
        // Configurar el paint para los círculos
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 8f
        circlePaint.color = ContextCompat.getColor(context, R.color.verde_completado)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calcular el rectángulo para las imágenes (dividimos en dos partes)
        val imageHeight = h.toFloat()
        val imageWidth = w / 2f
        
        imageRect.set(0f, 0f, imageWidth, imageHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar la imagen original a la izquierda
        imagenOriginal?.let {
            val leftRect = RectF(0f, 0f, width / 2f, height.toFloat())
            canvas.drawBitmap(it, null, leftRect, paint)
        }
        
        // Dibujar la imagen con diferencias a la derecha
        imagenDiferencias?.let {
            val rightRect = RectF(width / 2f, 0f, width.toFloat(), height.toFloat())
            canvas.drawBitmap(it, null, rightRect, paint)
        }
        
        // Dibujar línea divisoria
        val linePaint = Paint()
        linePaint.color = ContextCompat.getColor(context, R.color.rojo_activo)
        linePaint.strokeWidth = 4f
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), linePaint)
        
        // Dibujar círculos en las diferencias encontradas
        diferenciasEncontradas.forEach { index ->
            val diferencia = diferencias[index]
            val centerX = width / 2f + (diferencia.left + diferencia.right) / 2f * (width / 2f)
            val centerY = (diferencia.top + diferencia.bottom) / 2f * height
            val radius = 50f
            
            canvas.drawCircle(centerX, centerY, radius, circlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            // Solo procesar toques en la imagen de la derecha (con diferencias)
            if (x > width / 2f) {
                // Convertir coordenadas a relativas (0.0-1.0)
                val relativeX = (x - width / 2f) / (width / 2f)
                val relativeY = y / height
                
                // Verificar si se tocó una diferencia
                diferencias.forEachIndexed { index, diferencia ->
                    if (!diferenciasEncontradas.contains(index)) {
                        if (relativeX >= diferencia.left && relativeX <= diferencia.right &&
                            relativeY >= diferencia.top && relativeY <= diferencia.bottom) {
                            
                            // Diferencia encontrada
                            diferenciasEncontradas.add(index)
                            onDiferenciaEncontrada?.invoke()
                            invalidate()
                            return true
                        }
                    }
                }
            }
        }
        return true
    }
}
