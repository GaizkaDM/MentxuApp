package com.gaizkafrost.mentxuapp.Parada5

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Data class representing a step in the fishing process.
 * 
 * @param id Unique identifier (1 to 7) which also determines the correct order.
 * @param title Description text for the step.
 * @param imageRes Resource ID for the image (e.g., R.drawable.pesca_1).
 * @param userOrder The order assigned by the user (used in numeric mode).
 */
data class FishingStep(
    val id: Int,
    @StringRes val titleRes: Int,
    @DrawableRes val imageRes: Int,
    var userOrder: String = "" // For Modo A - Numérico
)
