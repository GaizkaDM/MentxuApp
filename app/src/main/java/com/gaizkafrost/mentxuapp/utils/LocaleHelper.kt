package com.gaizkafrost.mentxuapp.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import java.util.Locale

/**
 * Helper para gestionar el cambio de idioma en la aplicación en tiempo de ejecución.
 * Se encarga de actualizar la configuración de recursos y guardar la preferencia del usuario.
 *
 * @author Diego, Gaizka, Xiker
 */
object LocaleHelper {

    /**
     * Debe llamarse en el `attachBaseContext` de las Activities para aplicar el idioma guardado.
     * @param context El contexto base.
     * @return Un nuevo contexto con la configuración de local actualizada.
     */
    fun onAttach(context: Context): Context {
        val prefs = UserPreferences(context)
        val lang = prefs.language ?: "eu" // Por defecto Euskera
        return setLocale(context, lang)
    }

    /**
     * Establece un nuevo idioma y actualiza los recursos.
     * @param context Contexto de la aplicación.
     * @param language Código del idioma (ej: "eu", "es").
     * @return Contexto actualizado.
     */
    fun setLocale(context: Context, language: String): Context {
        val prefs = UserPreferences(context)
        prefs.language = language

        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
