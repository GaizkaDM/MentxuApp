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
    /**
     * Referencias a los elementos de Deskribapenak (adivinanzas).
     */
    private lateinit var deskribapena1: TextView
    private lateinit var deskribapena2: TextView
    private lateinit var deskribapena3: TextView
    private lateinit var deskribapena4: TextView
    private lateinit var deskribapena5: TextView
    private lateinit var deskribapena6: TextView
    private lateinit var deskribapena7: TextView

    /**
     * Referencias a los elementos de objetos donde se pueden soltar las adivinanzas.
     */
    private lateinit var objeto1: TextView
    private lateinit var objeto2: TextView
    private lateinit var objeto3: TextView
    private lateinit var objeto4: TextView
    private lateinit var objeto5: TextView
    private lateinit var objeto6: TextView
    private lateinit var objeto7: TextView

    /**
     * Contenedor principal de la actividad.
     */
    private lateinit var mainLayout: ConstraintLayout

    /**
     * Número de pares restantes para completar el juego.
     */
    private var remainingPairs = 7

    /**
     * Inicializa la actividad, configura la interfaz de usuario y establece los 
     * escuchadores de eventos para los elementos de arrastrar y soltar.
     *
     * @param savedInstanceState Si la actividad se está recreando a partir de un estado 
     * previo guardado, este es el estado.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relacionar)
        deskribapena1 = findViewById(R.id.Deskribapena1)
        deskribapena2 = findViewById(R.id.Deskribapena2)
        deskribapena3 = findViewById(R.id.Deskribapena3)
        deskribapena4 = findViewById(R.id.Deskribapena4)
        deskribapena5 = findViewById(R.id.Deskribapena5)
        deskribapena6 = findViewById(R.id.Deskribapena6)
        deskribapena7 = findViewById(R.id.Deskribapena7)
        objeto1 = findViewById(R.id.objeto1)
        objeto2 = findViewById(R.id.objeto2)
        objeto3 = findViewById(R.id.objeto3)
        objeto4 = findViewById(R.id.objeto4)
        objeto5 = findViewById(R.id.objeto5)
        objeto6 = findViewById(R.id.objeto6)
        objeto7 = findViewById(R.id.objeto7)
        mainLayout = findViewById(R.id.main)

        // Configuramos el arrastre
        val deskViews = listOf(deskribapena1, deskribapena2, deskribapena3, deskribapena4, deskribapena5, deskribapena6, deskribapena7)
        for (view in deskViews) {
            setDraggable(view)
        }

        // Configuración aleatoria de textos y emparejamientos
        setupGame()
    }

    /**
     * Configura el contenido de los TextViews de forma aleatoria y establece los vínculos correctos.
     */
    private fun setupGame() {
        val descriptions = listOf(
            R.string.deskribapena1, R.string.deskribapena2, R.string.deskribapena3,
            R.string.deskribapena4, R.string.deskribapena5, R.string.deskribapena6, R.string.deskribapena7
        )
        val objects = listOf(
            R.string.objeto1, R.string.objeto2, R.string.objeto3,
            R.string.objeto4, R.string.objeto5, R.string.objeto6, R.string.objeto7
        )

        val deskViews = listOf(deskribapena1, deskribapena2, deskribapena3, deskribapena4, deskribapena5, deskribapena6, deskribapena7)
        val objViews = listOf(objeto1, objeto2, objeto3, objeto4, objeto5, objeto6, objeto7)

        val shuffledDesks = descriptions.shuffled()
        val shuffledObjs = objects.shuffled()

        // Asignamos textos aleatorios
        deskViews.forEachIndexed { i, view -> view.text = getString(shuffledDesks[i]) }
        objViews.forEachIndexed { i, view -> view.text = getString(shuffledObjs[i]) }

        // Establecemos los vínculos de emparejamiento correctos
        for (i in descriptions.indices) {
            val deskText = getString(descriptions[i])
            val objText = getString(objects[i])

            val currentDeskView = deskViews.find { it.text == deskText }
            val currentObjView = objViews.find { it.text == objText }

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
