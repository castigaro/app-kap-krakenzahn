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

    /** Lautstärke der Hintergrundgeräusche (0..1). Bewusst leiser Standard,
     *  damit die Sprachausgabe verständlich bleibt. */
    fun ambienceVolume(context: Context): Float =
        prefs(context).getFloat("ambienceVolume", 0.15f)

    fun setAmbienceVolume(context: Context, volume: Float) {
        prefs(context).edit().putFloat("ambienceVolume", volume.coerceIn(0f, 1f)).apply()
    }

    fun isMusicEnabled(context: Context): Boolean =
        prefs(context).getBoolean("musicEnabled", true)

    fun setMusicEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("musicEnabled", enabled).apply()
    }

    /** Lautstärke der Hintergrundmusik (0..1). */
    fun musicVolume(context: Context): Float =
        prefs(context).getFloat("musicVolume", 0.10f)

    fun setMusicVolume(context: Context, volume: Float) {
        prefs(context).edit().putFloat("musicVolume", volume.coerceIn(0f, 1f)).apply()
    }

    /** Lautstärke der Soundeffekte (0..1). Vorher fest auf voller Lautstärke;
     *  der neue Standard senkt sie bewusst auf die Hälfte. */
    fun sfxVolume(context: Context): Float =
        prefs(context).getFloat("sfxVolume", 0.5f)

    fun setSfxVolume(context: Context, volume: Float) {
        prefs(context).edit().putFloat("sfxVolume", volume.coerceIn(0f, 1f)).apply()
    }

    /** Vollbild (Systemleisten ausgeblendet) im Spielbildschirm. */
    fun isFullscreen(context: Context): Boolean =
        prefs(context).getBoolean("fullscreen", false)

    fun setFullscreen(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("fullscreen", enabled).apply()
    }

    /** Ob die Verbtafel über der schmalen Dauer-Leiste aufgeklappt ist. */
    fun isPanelExpanded(context: Context): Boolean =
        prefs(context).getBoolean("panelExpanded", true)

    fun setPanelExpanded(context: Context, expanded: Boolean) {
        prefs(context).edit().putBoolean("panelExpanded", expanded).apply()
    }
}
