package com.gaizkafrost.mentxuapp.Parada5

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.R
import com.google.android.material.textfield.TextInputLayout

/**
 * Adapter for the fishing process steps list.
 *
 * @param steps List of steps to display.
 * @param isDragAndDropMode Flag to switch between Numeric (False) and Drag & Drop (True) mode.
 */
class FishingAdapter(
    private var steps: MutableList<FishingStep>,
    var isDragAndDropMode: Boolean = false
) : RecyclerView.Adapter<FishingAdapter.StepViewHolder>() {

    class StepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgStep: ImageView = view.findViewById(R.id.imgStep)
        val tvDescription: TextView = view.findViewById(R.id.tvStepDescription)
        val spinnerOrder: Spinner = view.findViewById(R.id.spinnerOrder)
        val imgDragHandle: ImageView = view.findViewById(R.id.imgDragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fishing_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[position]

        // Load image (Using R.drawable directly as requested)
        holder.imgStep.setImageResource(step.imageRes)
        holder.tvDescription.setText(step.titleRes)

        // Mode Switching UI Logic
        if (isDragAndDropMode) {
            holder.spinnerOrder.visibility = View.GONE
            holder.imgDragHandle.visibility = View.VISIBLE
        } else {
            holder.spinnerOrder.visibility = View.VISIBLE
            holder.imgDragHandle.visibility = View.GONE

            // Numeric Mode Logic: Setup Spinner
            val numbers = (0..steps.size).toList()
            val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, numbers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.spinnerOrder.adapter = adapter

            val currentSelection = step.userOrder.toIntOrNull() ?: 0
            holder.spinnerOrder.setSelection(currentSelection)

            holder.spinnerOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    step.userOrder = numbers[position].toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    step.userOrder = "0"
                }
            }
        }
    }

    override fun getItemCount(): Int = steps.size

    /**
     * Updates the list and refreshes the UI.
     */
    fun updateSteps(newSteps: List<FishingStep>) {
        this.steps = newSteps.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Logic for Drag & Drop: Swaps elements in the list.
     */
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = steps.removeAt(fromPosition)
        steps.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * Returns the current state of the steps list.
     */
    fun getItems(): List<FishingStep> = steps
}
