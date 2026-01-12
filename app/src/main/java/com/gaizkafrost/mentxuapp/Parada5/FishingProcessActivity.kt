package com.gaizkafrost.mentxuapp.Parada5

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.R

/**
 * Activity for the "Arrantzaren prozesua" (Fishing Process) minigame.
 * This screen allows students to order the phases of fishing.
 * 
 * DESIGN FOR TEACHERS:
 * - Change [stepsData] to edit titles or images.
 * - Change [IS_DRAG_AND_DROP_MODE] to switch interaction modes.
 * - [validateGame] contains the logic for checking correctness.
 */
class FishingProcessActivity : AppCompatActivity() {

    // --- CONFIGURATION FOR TEACHERS ---
    
    // Toggle between Mode A (Numeric = false) and Mode B (Drag & Drop = true)
    private val IS_DRAG_AND_DROP_MODE = false 

    // List of steps. You can add or remove steps here.
    // The "id" must be the correct sequence number (1, 2, 3...).
    private val stepsData = listOf(
        FishingStep(1, "Preparar el barco y las redes", R.drawable.pesca_1),
        FishingStep(2, "Navegar hacia el caladero", R.drawable.pesca_2),
        FishingStep(3, "Echar las redes al mar", R.drawable.pesca_3),
        FishingStep(4, "Recoger las redes con la captura", R.drawable.pesca_4),
        FishingStep(5, "Clasificar el pescado por especies", R.drawable.pesca_5),
        FishingStep(6, "Guardar en la bodega con hielo", R.drawable.pesca_6),
        FishingStep(7, "Llegada a puerto y subasta", R.drawable.pesca_7)
    )

    // ----------------------------------

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FishingAdapter
    private lateinit var tvFeedback: TextView
    private lateinit var btnCheck: Button
    private lateinit var btnReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fishing_process)

        // Initialize UI components
        recyclerView = findViewById(R.id.rvFishingSteps)
        tvFeedback = findViewById(R.id.tvFeedback)
        btnCheck = findViewById(R.id.btnCheck)
        btnReset = findViewById(R.id.btnReset)

        setupRecyclerView()

        btnCheck.setOnClickListener { validateGame() }
        btnReset.setOnClickListener { resetGame() }

        // Start the game by shuffling steps
        resetGame()
    }

    private fun setupRecyclerView() {
        // We use a shuffled copy for the initial state
        adapter = FishingAdapter(stepsData.shuffled().toMutableList(), IS_DRAG_AND_DROP_MODE)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // If Drag & Drop mode is active, we attach the ItemTouchHelper
        if (IS_DRAG_AND_DROP_MODE) {
            val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    rv: RecyclerView,
                    vh: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    adapter.moveItem(vh.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {}
            })
            touchHelper.attachToRecyclerView(recyclerView)
        }
    }

    /**
     * Logic to check if the user ordered the steps correctly.
     */
    private fun validateGame() {
        val currentItems = adapter.getItems()
        var isCorrect = true

        if (IS_DRAG_AND_DROP_MODE) {
            // Implementation Mode B: Check position in the list
            for (i in currentItems.indices) {
                if (currentItems[i].id != i + 1) {
                    isCorrect = false
                    break
                }
            }
        } else {
            // Implementation Mode A: Check the numbers written by the user
            for (step in currentItems) {
                val userNumber = step.userOrder.toIntOrNull()
                if (userNumber != step.id) {
                    isCorrect = false
                    break
                }
            }
            // Extra check: Ensure all fields are filled
            if (currentItems.any { it.userOrder.isEmpty() }) {
                showFeedback("Por favor, completa todos los números.", false)
                return
            }
        }

        if (isCorrect) {
            showFeedback("¡Muy bien! Has ordenado correctamente el proceso de la pesca.", true)
            Toast.makeText(this, "¡Excelente trabajo!", Toast.LENGTH_SHORT).show()
        } else {
            showFeedback("Hay algunos pasos mal ordenados. Vuelve a intentarlo.", false)
        }
    }

    /**
     * Resets the game state: reshuffles and clears feedback/inputs.
     */
    private fun resetGame() {
        val shuffledSteps = stepsData.shuffled().map { 
            it.copy(userOrder = "") // Create new instances to clear inputs
        }
        adapter.updateSteps(shuffledSteps)
        tvFeedback.visibility = View.GONE
        Toast.makeText(this, "Juego reiniciado. ¡A por ello!", Toast.LENGTH_SHORT).show()
    }

    private fun showFeedback(message: String, success: Boolean) {
        tvFeedback.text = message
        tvFeedback.visibility = View.VISIBLE
        val colorRes = if (success) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        tvFeedback.setTextColor(ContextCompat.getColor(this, colorRes))
    }
}
