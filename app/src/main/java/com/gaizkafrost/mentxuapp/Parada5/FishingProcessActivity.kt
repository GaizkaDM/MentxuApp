package com.gaizkafrost.mentxuapp.Parada5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.ParadasRepository
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
class FishingProcessActivity : BaseMenuActivity() {

    // --- CONFIGURATION FOR TEACHERS ---
    
    // Toggle between Mode A (Numeric = false) and Mode B (Drag & Drop = true)
    private val IS_DRAG_AND_DROP_MODE = false 

    // List of steps. You can add or remove steps here.
    // The "id" must be the correct sequence number (1, 2, 3...).
    private val stepsData = listOf(
        FishingStep(1, "Ontzia eta sareak prestatu", R.drawable.pesca_1),
        FishingStep(2, "Arrantza-lekura nabigatu", R.drawable.pesca_2),
        FishingStep(3, "Sareak itsasora bota", R.drawable.pesca_3),
        FishingStep(4, "Sareak jaso arrantzarekin", R.drawable.pesca_4),
        FishingStep(5, "Arraina espezieen arabera sailkatu", R.drawable.pesca_5),
        FishingStep(6, "Sotoan gorde izotzarekin", R.drawable.pesca_6),
        FishingStep(7, "Portura iritsi eta enkantea egin", R.drawable.pesca_7)
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
                showFeedback("Mesedez, bete zenbaki guztiak.", false)
                return
            }
        }

        if (isCorrect) {
            showFeedback("Oso ondo! Arrantza prozesua ondo ordenatu duzu.", true)
            Toast.makeText(this, "Lan bikaina!", Toast.LENGTH_SHORT).show()

            // Marcar la parada como completada
            val idParadaActual = intent.getIntExtra("ID_PARADA", 5)
            ParadasRepository.completarParada(idParadaActual)

            // Cerrar la actividad después de 2 segundos para dar tiempo a ver el mensaje
            recyclerView.postDelayed({
                finish()
            }, 2000)
        } else {
            showFeedback("Urrats batzuk gaizki ordenatuta daude. Saiatu berriro.", false)
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
        Toast.makeText(this, "Jokoa berrabiarazi da. Aurrera!", Toast.LENGTH_SHORT).show()

    }

    private fun showFeedback(message: String, success: Boolean) {
        tvFeedback.text = message
        tvFeedback.visibility = View.VISIBLE
        val colorRes = if (success) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        tvFeedback.setTextColor(ContextCompat.getColor(this, colorRes))
    }
}
