package com.castigaro.kapkrakenzahn.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Kleine App-Einstellungen (Audio-Schalter). Die KI-Provider-Einstellungen
 * verwaltet die Common Lib ([com.castigaro.common.llm.ProviderSettings])
 * in derselben Prefs-Datei.
 */
object AppSettings {

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isTtsEnabled(context: Context): Boolean =
        prefs(context).getBoolean("ttsEnabled", true)

    fun setTtsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("ttsEnabled", enabled).apply()
    }

    fun isSfxEnabled(context: Context): Boolean =
        prefs(context).getBoolean("sfxEnabled", true)

    fun setSfxEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("sfxEnabled", enabled).apply()
    }

    fun isAmbienceEnabled(context: Context): Boolean =
        prefs(context).getBoolean("ambienceEnabled", true)

    fun setAmbienceEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("ambienceEnabled", enabled).apply()
    }
}
