package com.gaizkafrost.mentxuapp.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context): Context {
        val prefs = UserPreferences(context)
        val lang = prefs.language ?: "eu"
        return setLocale(context, lang)
    }

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
