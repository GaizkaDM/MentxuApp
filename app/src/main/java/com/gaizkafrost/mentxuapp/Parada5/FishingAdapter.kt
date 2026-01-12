package com.gaizkafrost.mentxuapp.Parada5

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
        val etOrder: EditText = view.findViewById(R.id.etOrder)
        val tilOrder: TextInputLayout = view.findViewById(R.id.tilOrder)
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
        holder.tvDescription.text = step.title

        // Mode Switching UI Logic
        if (isDragAndDropMode) {
            holder.tilOrder.visibility = View.GONE
            holder.imgDragHandle.visibility = View.VISIBLE
        } else {
            holder.tilOrder.visibility = View.VISIBLE
            holder.imgDragHandle.visibility = View.GONE
            
            // Numeric Mode Logic: Update the userOrder field when text changes
            // Remove existing watchers to avoid loops
            holder.etOrder.tag = null 
            holder.etOrder.setText(step.userOrder)
            holder.etOrder.tag = step
            
            holder.etOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val stepInTag = holder.etOrder.tag as? FishingStep
                    stepInTag?.userOrder = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
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
