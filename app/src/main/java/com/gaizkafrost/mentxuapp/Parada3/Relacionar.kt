package com.gaizkafrost.mentxuapp.Parada3

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.gaizkafrost.mentxuapp.R

/**
 * Actividad principal que implementa un juego interactivo de emparejar (Drag & Drop).
 * 
 * Los usuarios deben arrastrar descripciones (deskribapenak) hacia sus correspondientes 
 * representaciones de objetos. Al completar todos los pares, se notifica el éxito.
 *
 * @author Diego
 * @version 1.1
 */
class Relacionar : AppCompatActivity() {

    private lateinit var deskViews: List<TextView>
    private lateinit var objViews: List<TextView>
    private var remainingPairs = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relacionar)

        // Inicializamos las listas de vistas usando sus IDs
        val deskIds = listOf(
            R.id.Deskribapena1, R.id.Deskribapena2, R.id.Deskribapena3,
            R.id.Deskribapena4, R.id.Deskribapena5, R.id.Deskribapena6, R.id.Deskribapena7
        )
        val objIds = listOf(
            R.id.objeto1, R.id.objeto2, R.id.objeto3,
            R.id.objeto4, R.id.objeto5, R.id.objeto6, R.id.objeto7
        )

        deskViews = deskIds.map { findViewById<TextView>(it) }
        objViews = objIds.map { findViewById<TextView>(it) }

        // Configuramos el arrastre
        deskViews.forEach { setDraggable(it) }

        // Configuración aleatoria de textos y emparejamientos
        setupGame()
    }

    /**
     * Configura el contenido de los TextViews de forma aleatoria y establece los vínculos correctos.
     */
    private fun setupGame() {
        val stringDeskIds = listOf(
            R.string.deskribapena1, R.string.deskribapena2, R.string.deskribapena3,
            R.string.deskribapena4, R.string.deskribapena5, R.string.deskribapena6, R.string.deskribapena7
        )
        val stringObjIds = listOf(
            R.string.objeto1, R.string.objeto2, R.string.objeto3,
            R.string.objeto4, R.string.objeto5, R.string.objeto6, R.string.objeto7
        )

        val shuffledDesks = stringDeskIds.shuffled()
        val shuffledObjs = stringObjIds.shuffled()

        // Asignamos textos aleatorios a las vistas
        deskViews.forEachIndexed { i, view -> view.text = getString(shuffledDesks[i]) }
        objViews.forEachIndexed { i, view -> view.text = getString(shuffledObjs[i]) }

        // Establecemos los vínculos de emparejamiento correctos basándonos en los IDs originales
        for (i in stringDeskIds.indices) {
            val correctDeskText = getString(stringDeskIds[i])
            val correctObjText = getString(stringObjIds[i])

            val currentDeskView = deskViews.find { it.text == correctDeskText }
            val currentObjView = objViews.find { it.text == correctObjText }

            if (currentDeskView != null && currentObjView != null) {
                setDroppable(currentObjView, currentDeskView)
            }
        }
    }

    /**
     * Habilita la funcionalidad de arrastre para un TextView proporcionado.
     * 
     * Utiliza [ClipData] y [View.DragShadowBuilder] para gestionar la representación 
     * visual del elemento durante el movimiento.
     *
     * @param view El TextView que el usuario podrá arrastrar.
     */
    private fun setDraggable(view: TextView) {
        view.setOnTouchListener { v, event ->
            val clipData = ClipData.newPlainText("", "")
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clipData, shadow, v, 0)
            true
        }
    }

    /**
     * Configura un TextView como destino de soltado (drop target) y define su 
     * correspondencia lógica con una descripción.
     *
     * Gestiona los eventos de arrastre:
     * - [DragEvent.ACTION_DRAG_ENTERED]: Cambia la transparencia para dar feedback visual.
     * - [DragEvent.ACTION_DROP]: Valida si el elemento soltado es el correcto.
     *
     * @param target El TextView que actúa como receptor del soltado.
     * @param matchingView El TextView (descripción) que debe coincidir con este destino.
     */
    private fun setDroppable(target: TextView, matchingView: TextView) {
        target.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.alpha = 0.5f // Indicar que se puede soltar
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    v.alpha = 1.0f // Restaurar opacidad
                    true
                }

                DragEvent.ACTION_DROP -> {
                    v.alpha = 1.0f
                    val draggedView = event.localState as? View
                    if (draggedView == matchingView) {
                        // Si coincide, ocultamos ambas vistas
                        draggedView?.visibility = View.GONE
                        v.visibility = View.GONE
                        remainingPairs--
                        checkCompletion()
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    v.alpha = 1.0f
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Verifica si el juego ha finalizado.
     * 
     * Evalúa si [remainingPairs] ha llegado a cero y, en tal caso, 
     * muestra un mensaje de finalización mediante un [Toast].
     */
    private fun checkCompletion() {
        if (remainingPairs == 0) {
            Toast.makeText(this, "Zorionak! Jarduera garaitu duzu!", Toast.LENGTH_LONG).show()
            // Aquí se podría añadir un retraso y cerrar la actividad o pasar a la siguiente
            // finish() 
        }
    }
}
