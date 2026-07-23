package com.appsonar.kapkrakenzahn.data

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * Einfacher JSON-Datei-Speicher für die Spielstände — gleiche Bauart wie
 * bei den anderen AppSonar-Apps: für überschaubare Datenmengen robuster
 * und schlanker als eine Datenbank. Jede laufende Partie hat einen festen
 * Autosave-Eintrag; manuelle Sicherungen sind Klone mit eigenem Namen.
 */
object SaveStore {

    /** Name des automatischen Spielstands (einer pro Partie). */
    const val AUTOSAVE_NAME = "Automatisch"

    private var saves: MutableList<GameState>? = null

    /** Nur für Tests: verwirft den In-Memory-Cache, erzwingt Neuladen. */
    internal fun resetForTest() {
        saves = null
    }

    private fun file(context: Context): File = File(context.filesDir, "savegames.json")

    private fun load(context: Context): MutableList<GameState> {
        saves?.let { return it }
        val loaded = mutableListOf<GameState>()
        val f = file(context)
        if (f.exists()) {
            runCatching {
                val arr = JSONArray(f.readText())
                for (i in 0 until arr.length()) {
                    loaded.add(GameState.fromJson(arr.getJSONObject(i)))
                }
            }
        }
        saves = loaded
        return loaded
    }

    fun save(context: Context) {
        val arr = JSONArray()
        load(context).forEach { arr.put(it.toJson()) }
        file(context).writeText(arr.toString())
    }

    fun getAll(context: Context): List<GameState> =
        load(context).sortedByDescending { it.updatedAt }

    fun get(context: Context, id: String): GameState? =
        load(context).firstOrNull { it.id == id }

    fun add(context: Context, state: GameState) {
        load(context).add(state)
        save(context)
    }

    /** Schreibt den (bereits in der Liste liegenden) Stand mit frischem Zeitstempel weg. */
    fun update(context: Context, state: GameState) {
        state.updatedAt = System.currentTimeMillis()
        val list = load(context)
        val index = list.indexOfFirst { it.id == state.id }
        if (index >= 0) list[index] = state else list.add(state)
        save(context)
    }

    fun delete(context: Context, state: GameState) {
        load(context).removeAll { it.id == state.id }
        save(context)
    }

    /** Der jüngste Spielstand — für „Weiterspielen". */
    fun newest(context: Context): GameState? = getAll(context).firstOrNull()
}
