package com.gaizkafrost.mentxuapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.R

/**
 * Adapter para mostrar los avatares disponibles de Mentxu en un grid
 */
class AvatarAdapter(
    private val avatars: List<AvatarItem>,
    private val onAvatarSelected: (AvatarItem, Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private var selectedPosition: Int = 0

    data class AvatarItem(
        val id: String,
        val resourceId: Int,
        val name: String
    )

    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val viewSelection: View = itemView.findViewById(R.id.viewSelection)
        val cardAvatar: CardView = itemView.findViewById(R.id.cardAvatar)

        fun bind(avatar: AvatarItem, position: Int) {
            imgAvatar.setImageResource(avatar.resourceId)
            
            // Mostrar/ocultar borde de selección
            viewSelection.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
            
            // Efecto de escala para seleccionado
            val scale = if (position == selectedPosition) 1.1f else 1.0f
            cardAvatar.scaleX = scale
            cardAvatar.scaleY = scale
            
            cardAvatar.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onAvatarSelected(avatar, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position], position)
    }

    override fun getItemCount(): Int = avatars.size

    fun getSelectedAvatar(): AvatarItem? {
        return if (selectedPosition >= 0 && selectedPosition < avatars.size) {
            avatars[selectedPosition]
        } else null
    }

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    companion object {
        /**
         * Lista de avatares disponibles de Mentxu
         */
        fun getDefaultAvatars(): List<AvatarItem> {
            return listOf(
                AvatarItem("mentxu_default", R.drawable.mentxu_victoria, "Mentxu"),
                AvatarItem("mentxu_bombera", R.drawable.mentxu_bombera, "Mentxu Bombera"),
                AvatarItem("mentxu_mecanica", R.drawable.mentxu_mecanica, "Mentxu Mekanikoa"),
                AvatarItem("mentxu_medica", R.drawable.mentxu_medica, "Mentxu Medikua"),
                AvatarItem("mentxu_policia", R.drawable.mentxu_policia, "Mentxu Polizia"),
                AvatarItem("mentxu_profesor", R.drawable.mentxu_profesor, "Mentxu Irakaslea"),
            )
        }
    }
}
